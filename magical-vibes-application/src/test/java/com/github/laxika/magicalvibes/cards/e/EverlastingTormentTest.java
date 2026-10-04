package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.i.InquisitorsSnare;
import com.github.laxika.magicalvibes.cards.l.LoamdraggerGiant;
import com.github.laxika.magicalvibes.cards.t.ToilToRenown;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EverlastingTorment.class, GrizzlyBears.class, HillGiant.class,
        FlameJavelin.class, InquisitorsSnare.class, LoamdraggerGiant.class, ToilToRenown.class,
        SongOfTheDryads.class})
class EverlastingTormentTest extends BaseCardTest {

    @Test
    @DisplayName("All damage is dealt with wither: a non-wither creature's combat damage becomes -1/-1 counters")
    void allDamageDealtWithWither() {
        // Attacker at battlefield index 0 (blocking targets reference the attacker's battlefield index).
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2, no native wither
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new EverlastingTorment());

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant()); // 3/3 → survives, so counters persist
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 2 power dealt as -1/-1 counters rather than marked damage; blocker survives as a 1/1.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Players can't gain life")
    void playersCantGainLife() {
        harness.addToBattlefield(player1, new EverlastingTorment());

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Damage can't be prevented")
    void damageCantBePrevented() {
        harness.addToBattlefield(player1, new EverlastingTorment());

        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }

    @Test
    void spellDamageToCreaturesBecomesCountersForEitherController() {
        harness.addToBattlefield(player1, new EverlastingTorment());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LoamdraggerGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LoamdraggerGiant());

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
    }

    @Test
    void witherSpellDamageToPlayerStillCausesLifeLoss() {
        harness.addToBattlefield(player2, new EverlastingTorment());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void lifeGainResumesAfterTormentLeavesBattlefield() {
        Permanent torment = harness.addToBattlefieldAndReturn(player1, new EverlastingTorment());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LoamdraggerGiant());
        creature.tap();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new ToilToRenown(), new ToilToRenown()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);

        gd.playerBattlefields.get(player1.getId()).remove(torment);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.assertLife(player2, 21);
    }

    @Test
    void combatDamageIgnoresPreventionFromResolvedSnare() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new EverlastingTorment());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new InquisitorsSnare()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();
        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
    }

    @Test
    void spellDamageIsMarkedNormallyAfterTormentLeavesBattlefield() {
        Permanent torment = harness.addToBattlefieldAndReturn(player1, new EverlastingTorment());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LoamdraggerGiant());
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(torment);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gqs.isDamagePreventable(gd)).isTrue();
    }

    @Test
    void lifeGainIsAllowedWhenTormentBecomesForest() {
        turnTormentIntoForest();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LoamdraggerGiant());
        creature.tap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ToilToRenown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void spellDamageIsMarkedNormallyWhenTormentBecomesForest() {
        turnTormentIntoForest();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LoamdraggerGiant());
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void combatDamageCanBePreventedWhenTormentBecomesForest() {
        turnTormentIntoForest();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new InquisitorsSnare()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();
        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
    }

    private void turnTormentIntoForest() {
        Permanent torment = harness.addToBattlefieldAndReturn(player2, new EverlastingTorment());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, torment.getId());
        harness.passBothPriorities();
    }
}
