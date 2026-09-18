package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiptideLaboratory.class, RiptideBiologist.class, GlorySeeker.class})
class RiptideLaboratoryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent laboratory = addReadyLaboratory();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(laboratory.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns a Wizard you control to its owner's hand")
    void returnsWizardYouControl() {
        addReadyLaboratory();
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new RiptideBiologist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, wizard.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Riptide Biologist");
        harness.assertInHand(player1, "Riptide Biologist");
    }

    @Test
    @DisplayName("Only a Wizard you control is a legal target")
    void rejectsNonWizardOrOpponentWizard() {
        addReadyLaboratory();
        Permanent nonWizard = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent opponentWizard = harness.addToBattlefieldAndReturn(player2, new RiptideBiologist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonWizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Wizard you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentWizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Wizard you control");
    }

    @Test
    @DisplayName("Returns a controlled Wizard to its owner's hand")
    void returnsControlledWizardToItsOwnersHand() {
        addReadyLaboratory();
        RiptideBiologist wizardCard = new RiptideBiologist();
        wizardCard.setOwnerId(player2.getId());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, wizardCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, wizard.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Riptide Biologist");
        harness.assertNotInHand(player1, "Riptide Biologist");
        harness.assertInHand(player2, "Riptide Biologist");
    }

    private Permanent addReadyLaboratory() {
        Permanent laboratory = harness.addToBattlefieldAndReturn(player1, new RiptideLaboratory());
        laboratory.setSummoningSick(false);
        return laboratory;
    }
}
