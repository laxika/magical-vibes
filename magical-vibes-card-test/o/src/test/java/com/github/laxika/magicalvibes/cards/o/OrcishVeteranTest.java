package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishVeteran.class, IcatianPhalanx.class, IcatianInfantry.class, Orgg.class})
class OrcishVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Can't block white creatures with power 2 or greater, but can block other creatures")
    void restrictsBlockingByColorAndPower() {
        Permanent veteran = addVeteran();
        Permanent whiteTwoPower = addCreatureReady(player2, new IcatianPhalanx());
        Permanent whiteOnePower = addCreatureReady(player2, new IcatianInfantry());
        Permanent nonwhiteTwoPower = addCreatureReady(player2, new Orgg());

        assertThat(bls.canBlockAttacker(gd, veteran, whiteTwoPower,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, veteran, whiteOnePower,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, veteran, nonwhiteTwoPower,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Uses the effective power of a white creature for the blocking restriction")
    void restrictsBlockingByEffectivePower() {
        Permanent veteran = addVeteran();
        Permanent whiteCreature = addCreatureReady(player2, new IcatianInfantry());
        whiteCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(bls.canBlockAttacker(gd, veteran, whiteCreature,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("The activated ability grants first strike until end of turn")
    void gainsFirstStrikeUntilEndOfTurn() {
        Permanent veteran = addCreatureReady(player1, new OrcishVeteran());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Can block a white creature whose effective power has fallen below two")
    void canBlockWhiteCreatureWithReducedPower() {
        Permanent veteran = addVeteran();
        Permanent attacker = addCreatureReady(player2, new IcatianPhalanx());
        attacker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(bls.canBlockAttacker(gd, veteran, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick, granting first strike only to itself")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent veteran = addVeteran();
        Permanent otherVeteran = addVeteran();
        veteran.tap();
        veteran.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherVeteran, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(veteran.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First strike kills an opposing veteran before it can deal combat damage")
    void firstStrikePreventsLethalReturnDamage() {
        Permanent attacker = addVeteran();
        Permanent blocker = addCreatureReady(player2, new OrcishVeteran());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Orcish Veteran");
    }

    private Permanent addVeteran() {
        return addCreatureReady(player1, new OrcishVeteran());
    }
}
