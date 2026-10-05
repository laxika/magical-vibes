package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WrennAndSix;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishHellraiser.class, Murder.class, GrizzlyBears.class, WrennAndSix.class})
class OrcishHellraiserTest extends BaseCardTest {

    @Test
    @DisplayName("When Orcish Hellraiser dies, it deals 2 damage to the chosen player")
    void deathTriggerDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new OrcishHellraiser());
        killHellraiser();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The death trigger can target a planeswalker but not a creature")
    void deathTriggerTargetsPlaneswalker() {
        harness.addToBattlefield(player1, new OrcishHellraiser());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new WrennAndSix());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID planeswalkerId = planeswalker.getId();

        killHellraiser();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(planeswalkerId)
                .doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("Declining echo sacrifices Orcish Hellraiser")
    void decliningEchoSacrificesHellraiser() {
        castHellraiser();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Orcish Hellraiser");
        harness.assertInGraveyard(player1, "Orcish Hellraiser");
    }

    @Test
    @DisplayName("Paying echo keeps Orcish Hellraiser on the battlefield")
    void payingEchoKeepsHellraiser() {
        castHellraiser();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Orcish Hellraiser");
    }

    @Test
    @DisplayName("Echo does not create an enter-the-battlefield trigger")
    void enteringDoesNotPutEchoRegistrationOnStack() {
        harness.castFromHand(player1, new OrcishHellraiser(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Orcish Hellraiser");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing to echo triggers damage and can target its controller")
    void echoSacrificeDealsDamageToController() {
        castHellraiser();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Orcish Hellraiser");
    }

    @Test
    @DisplayName("The death trigger removes two loyalty counters from a real planeswalker")
    void deathTriggerDamagesPlaneswalker() {
        Permanent wrenn = harness.addToBattlefieldAndReturn(player2, new WrennAndSix());
        wrenn.setCounterCount(CounterType.LOYALTY, 3);
        castHellraiser();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, wrenn.getId());
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Echo waits for its controller's upkeep and is not charged again after payment")
    void echoOnlyTriggersAtFirstControllerUpkeep() {
        castHellraiser();
        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Orcish Hellraiser");
    }

    private void castHellraiser() {
        harness.castFromHand(player1, new OrcishHellraiser(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void killHellraiser() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        UUID hellraiserId = harness.getPermanentId(player1, "Orcish Hellraiser");
        harness.castInstant(player2, 0, hellraiserId);
        harness.passBothPriorities();
    }

}
