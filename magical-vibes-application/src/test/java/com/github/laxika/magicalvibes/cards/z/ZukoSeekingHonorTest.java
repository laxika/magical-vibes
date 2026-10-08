package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZukoSeekingHonor.class, GrizzlyBears.class, Shock.class})
class ZukoSeekingHonorTest extends BaseCardTest {

    @Test
    void firebendingAddsRedManaUntilEndOfCombat() {
        addReadyZuko();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void castingANoncreatureSpellGivesFirstStrikeUntilEndOfTurn() {
        Permanent zuko = addReadyZuko();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.hasKeyword(gd, zuko, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, zuko, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void castingACreatureSpellDoesNotGiveFirstStrike() {
        Permanent zuko = addReadyZuko();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, zuko, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void dealingCombatDamagePutsAPlusOnePlusOneCounterOnZuko() {
        Permanent zuko = addReadyZuko();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(zuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void anOpponentsNoncreatureSpellDoesNotGiveFirstStrike() {
        Permanent zuko = addReadyZuko();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, zuko, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(zuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    void castingANoncreatureSpellDuringAnOpponentsTurnGivesFirstStrike() {
        Permanent zuko = addReadyZuko();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, zuko, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(zuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    void firstStrikeKillsABlockerWithoutAwardingACombatDamageCounter() {
        Permanent zuko = addReadyZuko();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Zuko, Seeking Honor");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(zuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyZuko() {
        return addCreatureReady(player1, new ZukoSeekingHonor());
    }
}
