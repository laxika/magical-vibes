package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OonasBlackguard.class, ElvishWarrior.class})
class OonasBlackguardTest extends BaseCardTest {

    @Test
    @DisplayName("Another Rogue you control enters with an additional +1/+1 counter")
    void otherRogueEntersWithCounter() {
        addReadyBlackguard(player1);

        harness.setHand(player1, List.of(new OonasBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanents(player1, "Oona's Blackguard").get(1);
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Oona's Blackguard does not give itself a counter as it enters")
    void blackguardItselfDoesNotEnterWithCounter() {
        harness.setHand(player1, List.of(new OonasBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent blackguard = findPermanent(player1, "Oona's Blackguard");
        assertThat(blackguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent's Rogue does not benefit from your Oona's Blackguard")
    void opponentRogueDoesNotBenefit() {
        addReadyBlackguard(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new OonasBlackguard()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player2, "Oona's Blackguard");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Creature with a +1/+1 counter deals combat damage — that player discards a card")
    void counterCreatureCombatDamageForcesDiscard() {
        harness.setHand(player2, List.of(new ElvishWarrior()));
        addReadyBlackguard(player1);

        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities(); // resolve the discard trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Creature without a +1/+1 counter does not trigger the discard")
    void noCounterCreatureDoesNotTrigger() {
        harness.setHand(player2, List.of(new ElvishWarrior()));
        addReadyBlackguard(player1);

        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Elvish Warrior");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No discard when the damaged player has an empty hand")
    void noDiscardWhenEmptyHand() {
        harness.setHand(player2, List.of());
        addReadyBlackguard(player1);

        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no cards to discard")).isTrue();
    }

    @Test
    @DisplayName("Multiple Blackguards each add a counter to an entering Rogue")
    void multipleBlackguardsAddCounters() {
        addReadyBlackguard(player1);
        addReadyBlackguard(player1);
        harness.setHand(player1, List.of(new OonasBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Oona's Blackguard").get(2)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Rogue enters without an additional counter")
    void nonRogueEntersWithoutCounter() {
        addReadyBlackguard(player1);
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Elvish Warrior")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Blackguard's own combat damage triggers discard when it has a counter")
    void blackguardWithCounterTriggersForItself() {
        harness.setHand(player2, List.of(new ElvishWarrior(), new OonasBlackguard()));
        Permanent attacker = addReadyBlackguard(player1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Oona's Blackguard");
        harness.assertInHand(player2, "Elvish Warrior");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's creature with a counter does not trigger your Blackguard")
    void opponentCounterCreatureDoesNotTrigger() {
        addReadyBlackguard(player1);
        harness.setHand(player1, List.of(new ElvishWarrior()));
        Permanent attacker = addCreatureReady(player2, new ElvishWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Elvish Warrior");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyBlackguard(Player player) {
        return addCreatureReady(player, new OonasBlackguard());
    }
}
