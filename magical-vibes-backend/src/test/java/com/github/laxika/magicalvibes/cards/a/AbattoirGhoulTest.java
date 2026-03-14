package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbattoirGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Abattoir Ghoul has correct ON_DAMAGED_CREATURE_DIES effect")
    void hasCorrectProperties() {
        AbattoirGhoul card = new AbattoirGhoul();

        assertThat(card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES).getFirst())
                .isInstanceOf(GainLifeEqualToToughnessEffect.class);
    }

    @Test
    @DisplayName("Gains life equal to dying creature's toughness when it kills in combat")
    void gainsLifeWhenKillingInCombat() {
        harness.addToBattlefield(player1, new AbattoirGhoul());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ghoul = gd.playerBattlefields.get(player1.getId()).getFirst();
        ghoul.setSummoningSick(false);
        ghoul.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Pass through first strike damage, regular damage, and trigger resolution
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Grizzly Bears (toughness 2) should be dead
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        // Controller gains life equal to Grizzly Bears' toughness (2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Gains life equal to toughness when damaged creature dies later in the turn")
    void gainsLifeWhenDamagedCreatureDiesLater() {
        harness.addToBattlefield(player1, new AbattoirGhoul());

        // Use a creature with high toughness that survives combat
        GrizzlyBears toughBlocker = new GrizzlyBears();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(5);
        harness.addToBattlefield(player2, toughBlocker);

        Permanent ghoul = gd.playerBattlefields.get(player1.getId()).getFirst();
        ghoul.setSummoningSick(false);
        ghoul.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve first strike damage — blocker survives (3 damage on 5 toughness)
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Now kill the blocker with Cruel Edict later in the turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        // Pass 1: resolve Cruel Edict, creature dies, ON_DAMAGED_CREATURE_DIES trigger fires
        harness.passBothPriorities();
        // Pass 2: resolve triggered ability — gain life
        harness.passBothPriorities();

        // Blocker should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Controller gains life equal to the dying creature's toughness (5)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Does not gain life when a creature not damaged by it dies")
    void doesNotGainLifeForUndamagedCreature() {
        harness.addToBattlefield(player1, new AbattoirGhoul());
        harness.addToBattlefield(player2, new GrizzlyBears());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Kill the creature without Abattoir Ghoul dealing damage to it
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        // No life gained — Abattoir Ghoul didn't damage the creature
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
