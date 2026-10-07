package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.c.CosmiumBlast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SovereignOkinecAhau.class, GrizzlyBears.class, ArmoredKincaller.class,
        SoulsOfTheLost.class, CosmiumBlast.class})
class SovereignOkinecAhauTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on each creature equal to its power above base power")
    void putsCountersEqualToPowerAboveBasePower() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        Permanent oneAboveBase = addCreatureReady(player1, new GrizzlyBears());
        oneAboveBase.setPowerModifier(1);
        Permanent threeAboveBase = addCreatureReady(player1, new GrizzlyBears());
        threeAboveBase.setPowerModifier(3);
        Permanent unmodified = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sovereign)));
        resolveAllTriggers();

        assertThat(oneAboveBase.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(threeAboveBase.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(unmodified.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sovereign.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Uses current power and base power at trigger resolution")
    void usesResolutionCharacteristics() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sovereign)));
        creature.setPowerModifier(2);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void includesExistingCountersAndSovereignButNotOpponentsOrReducedPower() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        sovereign.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent creature = addCreatureReady(player1, new ArmoredKincaller());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        creature.setPowerModifier(1);
        Permanent reduced = addCreatureReady(player1, new ArmoredKincaller());
        reduced.setPowerModifier(-1);
        Permanent opponent = addCreatureReady(player2, new ArmoredKincaller());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(sovereign.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(reduced.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void characteristicDefiningPowerIsBasePower() {
        addCreatureReady(player1, new SovereignOkinecAhau());
        harness.setGraveyard(player1, List.of(new ArmoredKincaller(), new ArmoredKincaller()));
        Permanent unmodified = addCreatureReady(player1, new SoulsOfTheLost());
        Permanent modified = addCreatureReady(player1, new SoulsOfTheLost());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(unmodified.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(modified.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void basePowerSettingDoesNotCountAsPowerBonus() {
        addCreatureReady(player1, new SovereignOkinecAhau());
        Permanent creature = addCreatureReady(player1, new ArmoredKincaller());
        creature.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
        creature.setBasePowerOverride(6);
        creature.setBaseToughnessOverride(6);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void noCountersWhenPowerBonusDisappearsBeforeResolution() {
        addCreatureReady(player1, new SovereignOkinecAhau());
        Permanent creature = addCreatureReady(player1, new ArmoredKincaller());
        creature.setPowerModifier(2);

        declareAttackers(List.of(0));
        creature.setPowerModifier(0);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerResolvesAfterSovereignLeavesBattlefield() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        Permanent creature = addCreatureReady(player1, new ArmoredKincaller());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(sovereign);
        gd.playerGraveyards.get(player1.getId()).add(sovereign.getCard());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void wardCountersOpponentSpellWhenTheyCannotPay() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        sovereign.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new CosmiumBlast()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player2, 0, sovereign.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sovereign Okinec Ahau");
        assertThat(sovereign.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Cosmium Blast");
    }

    @Test
    void wardDoesNotCounterControllersOwnSpell() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        sovereign.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new CosmiumBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, sovereign.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sovereign Okinec Ahau");
        harness.assertInGraveyard(player1, "Cosmium Blast");
    }

    @Test
    void payingWardAllowsOpponentSpellToResolve() {
        Permanent sovereign = addCreatureReady(player1, new SovereignOkinecAhau());
        sovereign.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new CosmiumBlast()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castInstant(player2, 0, sovereign.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sovereign Okinec Ahau");
        harness.assertInGraveyard(player2, "Cosmium Blast");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void anotherCreatureAttackingDoesNotTriggerSovereign() {
        addCreatureReady(player1, new SovereignOkinecAhau());
        Permanent creature = addCreatureReady(player1, new ArmoredKincaller());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
