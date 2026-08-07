package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScabClanBerserkerTest extends BaseCardTest {

    /** Puts a Berserker under player1 and hands the turn to the opponent, ready to cast. */
    private Permanent setUpOpponentTurn(boolean renowned) {
        Permanent berserker = addCreatureReady(player1, new ScabClanBerserker());
        berserker.setRenowned(renowned);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return berserker;
    }

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent berserker = addCreatureReady(player1, new ScabClanBerserker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(berserker.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renowned: an opponent's noncreature spell deals 2 damage to that player")
    void renownedNoncreatureSpellDealsDamage() {
        setUpOpponentTurn(true);
        harness.setHand(player2, List.of(new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Scab-Clan Berserker");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Not renowned: the intervening-if stops the ability from triggering at all")
    void notRenownedDoesNotTrigger() {
        setUpOpponentTurn(false);
        harness.setHand(player2, List.of(new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Renowned: an opponent's creature spell does not trigger the damage")
    void creatureSpellDoesNotTrigger() {
        setUpOpponentTurn(true);
        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Renowned: the controller's own noncreature spell does not trigger the damage")
    void ownSpellDoesNotTrigger() {
        addCreatureReady(player1, new ScabClanBerserker()).setRenowned(true);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }
}
