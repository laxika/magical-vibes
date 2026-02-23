package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SengirVampireTest extends BaseCardTest {


    @Test
    @DisplayName("Sengir Vampire has correct card properties")
    void hasCorrectProperties() {
        SengirVampire card = new SengirVampire();

        assertThat(card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES).getFirst())
                .isInstanceOf(PutCountersOnSourceEffect.class);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDiesInCombat() {
        harness.addToBattlefield(player1, new SengirVampire());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent sengir = gd.playerBattlefields.get(player1.getId()).getFirst();
        sengir.setSummoningSick(false);
        sengir.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(sengir.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, sengir)).isEqualTo(5);
    }

    @Test
    @DisplayName("Triggers when a creature damaged by Sengir Vampire dies later the same turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        harness.addToBattlefield(player1, new SengirVampire());

        GrizzlyBears toughBlocker = new GrizzlyBears();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(5);
        harness.addToBattlefield(player2, toughBlocker);

        Permanent sengir = gd.playerBattlefields.get(player1.getId()).getFirst();
        sengir.setSummoningSick(false);
        sengir.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(sengir.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(sengir.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, sengir)).isEqualTo(5);
    }
}
