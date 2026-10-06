package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    @DisplayName("A newly entered Laboratory can tap for mana")
    void newlyEnteredLandCanTapForMana() {
        Permanent laboratory = harness.addToBattlefieldAndReturn(player1, new RiptideLaboratory());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(laboratory.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning a Wizard pays mana and taps the Laboratory before resolution")
    void bouncePaysCostsBeforeResolution() {
        Permanent laboratory = addReadyLaboratory();
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new RiptideBiologist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, wizard.getId());

        assertThat(laboratory.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertOnBattlefield(player1, "Riptide Biologist");
        harness.assertNotInHand(player1, "Riptide Biologist");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Riptide Biologist");
        harness.assertInHand(player1, "Riptide Biologist");
    }

    @Test
    @DisplayName("A Laboratory tapped for mana cannot also return a Wizard")
    void cannotBounceAfterTappingForMana() {
        addReadyLaboratory();
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new RiptideBiologist());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Riptide Biologist");
    }

    @Test
    @DisplayName("A Wizard that changes controllers before resolution is no longer legal")
    void doesNotReturnWizardNoLongerControlled() {
        Permanent laboratory = addReadyLaboratory();
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new RiptideBiologist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, wizard.getId());

        gd.playerBattlefields.get(player1.getId()).remove(wizard);
        gd.playerBattlefields.get(player2.getId()).add(wizard);
        gd.stolenCreatures.put(wizard.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Riptide Biologist");
        harness.assertNotInHand(player1, "Riptide Biologist");
        harness.assertNotInHand(player2, "Riptide Biologist");
        assertThat(laboratory.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A face-down Wizard is not a legal target")
    void rejectsFaceDownWizard() {
        addReadyLaboratory();
        harness.setHand(player1, List.of(new RiptideBiologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent faceDownWizard = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, faceDownWizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Wizard you control");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faceDownWizard);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLaboratory() {
        Permanent laboratory = harness.addToBattlefieldAndReturn(player1, new RiptideLaboratory());
        laboratory.setSummoningSick(false);
        return laboratory;
    }
}
