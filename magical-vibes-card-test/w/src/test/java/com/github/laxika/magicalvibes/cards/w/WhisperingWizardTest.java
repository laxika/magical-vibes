package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhisperingWizard.class, Shock.class, GrizzlyBears.class})
class WhisperingWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a flying Spirit token")
    void noncreatureSpellCreatesSpiritToken() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Spirit"), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        castShock();
        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersAgainOnLaterTurn() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        castShock();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Spirit token")
    void creatureSpellDoesNotCreateSpiritToken() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger the Wizard or consume its trigger")
    void opponentSpellDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isZero();
        castShock();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature spell does not consume the noncreature trigger")
    void creatureSpellDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Wizard triggers independently once each turn")
    void multipleWizardsTriggerIndependently() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.addToBattlefield(player1, new WhisperingWizard());

        castShock();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        castShock();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature spells cast before the Wizard enters do not consume its trigger")
    void earlierSpellDoesNotConsumeTrigger() {
        castShock();
        harness.addToBattlefield(player1, new WhisperingWizard());

        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("A second spell cannot trigger while the first trigger is still on the stack")
    void pendingTriggerConsumesTurnLimit() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("The Wizard also triggers during an opponent's turn")
    void triggersDuringOpponentTurn() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.forceActivePlayer(player2);

        castShock();
        castShock();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("The pending ability creates a Spirit even if the Wizard dies before resolution")
    void triggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new WhisperingWizard());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Whispering Wizard"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Whispering Wizard");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    private void castShock() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
    }
}
