package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
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
        Permanent attacker = readyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());

        resolveCombatDamage();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The fourth quest counter also draws a card")
    void fourthQuestCounterDrawsCard() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 3);
        Permanent attacker = readyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        resolveCombatDamage();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A creature an opponent controls does not add a quest counter")
    void opponentCreatureDoesNotTrigger() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        Permanent attacker = readyCreature(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombatDamage();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Waterbend makes a target creature unable to be blocked this turn")
    void waterbendMakesTargetCreatureUnblockable() {
        harness.addToBattlefieldAndReturn(player1, new WaterbenderAscension());
        Permanent attacker = readyCreature(player1, new GrizzlyBears());
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareBlockers();

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

    private Permanent readyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
