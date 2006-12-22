/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.mylar.ui.wizard;

import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylar.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;

public class JtracRepositorySettingsPage extends AbstractRepositorySettingsPage {

	public JtracRepositorySettingsPage(AbstractRepositoryConnectorUi repositoryUi) {
		super("JTrac Connection", "Repository Connection Settings", repositoryUi);
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		
	}

	@Override
	protected boolean isValidUrl(String name) { 
		return true;
	}

	@Override
	protected void validateSettings() {
		
	}

}