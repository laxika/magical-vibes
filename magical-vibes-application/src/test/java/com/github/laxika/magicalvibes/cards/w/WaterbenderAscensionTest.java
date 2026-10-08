package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterbenderAscension.class, GrizzlyBears.class, Island.class})
class WaterbenderAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature you control adds a quest counter")
    void combatDamageAddsQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The fourth quest counter also draws a card")
    void fourthQuestCounterDrawsCard() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 3);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombat();
        resolveAllTriggers();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A creature an opponent controls does not add a quest counter")
    void opponentCreatureDoesNotTrigger() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Waterbend makes a target creature unable to be blocked this turn")
    void waterbendMakesTargetCreatureUnblockable() {
        harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Waterbend can target only a creature")
    void waterbendRejectsNonCreatureTarget() {
        harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Each creature dealing combat damage creates its own counter and draw check")
    void simultaneousCombatDamageChecksThresholdForEachCreature() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 2);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Card drawn = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombat();
        resolveAllTriggers();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Combat damage above the threshold continues to draw cards")
    void countersAboveFourStillDraw() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 5);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Card drawn = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombat();
        resolveAllTriggers();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A pending trigger uses the removed enchantment's last known quest counters")
    void removedAscensionWithFourCountersStillDraws() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 4);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Card drawn = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ascension));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Prevented combat damage does not add quest counters")
    void preventedCombatDamageDoesNotTrigger() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        gd.preventAllCombatDamage = true;

        resolveCombat();
        resolveAllTriggers();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A removed enchantment cannot gain its fourth quest counter")
    void removedAscensionWithThreeCountersDoesNotDraw() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 3);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Card undrawn = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(undrawn));
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ascension));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay waterbend and an opponent's creature can be targeted")
    void waterbendPaidWithSummoningSickCreatures() {
        harness.addToBattlefield(player1, new WaterbenderAscension());
        Permanent payer1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent payer2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent payer3 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent payer4 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(List.of(payer1, payer2, payer3, payer4)).allMatch(Permanent::isTapped);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
