package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PredatorOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Has attack and damaged-creature-dies counter triggers")
    void hasCounterTriggers() {
        PredatorOoze card = new PredatorOoze();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOfSatisfying(PutCountersOnSourceEffect.class, effect -> {
                    assertThat(effect.powerModifier()).isEqualTo(1);
                    assertThat(effect.toughnessModifier()).isEqualTo(1);
                    assertThat(effect.amount()).isEqualTo(1);
                });

        assertThat(card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES).getFirst())
                .isInstanceOfSatisfying(PutCountersOnSourceEffect.class, effect -> {
                    assertThat(effect.powerModifier()).isEqualTo(1);
                    assertThat(effect.toughnessModifier()).isEqualTo(1);
                    assertThat(effect.amount()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when it attacks")
    void getsCounterWhenAttacking() {
        Permanent ooze = addCreatureReady(player1, new PredatorOoze());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Predator Ooze"));

        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged dies in combat")
    void getsCounterWhenDamagedCreatureDiesInCombat() {
        harness.addToBattlefield(player1, new PredatorOoze());

        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        Permanent ooze = findPermanent(player1, "Predator Ooze");
        ooze.setSummoningSick(false);
        ooze.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ooze.getId()));
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers when a creature it damaged dies later the same turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        harness.addToBattlefield(player1, new PredatorOoze());

        GrizzlyBears toughCreature = new GrizzlyBears();
        toughCreature.setPower(1);
        toughCreature.setToughness(5);
        harness.addToBattlefield(player2, toughCreature);

        Permanent ooze = findPermanent(player1, "Predator Ooze");
        ooze.setSummoningSick(false);
        ooze.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an undamaged creature dies")
    void noTriggerWhenUndamagedCreatureDies() {
        harness.addToBattlefield(player1, new PredatorOoze());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ooze = findPermanent(player1, "Predator Ooze");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).noneMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Predator Ooze"));
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
