package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChorusOfTheConclave.class, Forest.class})
class ChorusOfTheConclaveTest extends BaseCardTest {

    @Test
    void givesAnyPlayersCreatureSpellAdditionalCountersForManaPaid() {
        harness.addToBattlefield(player1, new ChorusOfTheConclave());
        ChorusOfTheConclave creatureSpell = new ChorusOfTheConclave();
        castCreatureWithChorusPayment(player2, creatureSpell, 2);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureSpell.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    @Test
    void payingZeroIsAllowed() {
        harness.addToBattlefield(player1, new ChorusOfTheConclave());
        ChorusOfTheConclave creatureSpell = new ChorusOfTheConclave();
        castCreatureWithChorusPayment(player2, creatureSpell, 0);
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creatureSpell.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void paidCountersRemainIfChorusLeavesBeforeCreatureResolves() {
        Permanent chorus = harness.addToBattlefieldAndReturn(player1, new ChorusOfTheConclave());
        ChorusOfTheConclave creatureSpell = new ChorusOfTheConclave();
        castCreatureWithChorusPayment(player2, creatureSpell, 2);

        gd.playerBattlefields.get(player1.getId()).remove(chorus);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureSpell.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    @Test
    void forestwalkAllowsBlockingWhenDefenderControlsNoForest() {
        Permanent attacker = addCreatureReady(player1, new ChorusOfTheConclave());
        Permanent blocker = addCreatureReady(player2, new ChorusOfTheConclave());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void forestwalkPreventsBlockingWhenDefenderControlsAForest() {
        Permanent attacker = addCreatureReady(player1, new ChorusOfTheConclave());
        Permanent blocker = addCreatureReady(player2, new ChorusOfTheConclave());
        harness.addToBattlefield(player2, new Forest());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCreatureWithChorusPayment(Player player, Card card, int additionalGenericMana) {
        harness.setHand(player, List.of(card));
        addManaForChorus(player, additionalGenericMana);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        List<String> repeatedAdditionalCosts = java.util.Collections.nCopies(additionalGenericMana, "{1}");
        gs.playCard(gd, player, 0, 0, null, null,
                List.of(), List.of(), false, null, null, List.of(), null, null,
                false, null, null, List.of(), List.of(),
                repeatedAdditionalCosts, false);
    }

    private void addManaForChorus(Player player, int additionalGenericMana) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4 + additionalGenericMana);
    }
}
