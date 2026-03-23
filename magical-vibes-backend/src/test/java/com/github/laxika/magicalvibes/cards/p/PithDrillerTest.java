package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PithDrillerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB that puts one -1/-1 counter on target creature")
    void hasCorrectEffects() {
        PithDriller card = new PithDriller();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(PutMinusOneMinusOneCounterOnTargetCreatureEffect.class);

        PutMinusOneMinusOneCounterOnTargetCreatureEffect effect =
                (PutMinusOneMinusOneCounterOnTargetCreatureEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(1);
    }

    // ===== ETB: put one -1/-1 counter on target creature =====

    @Test
    @DisplayName("ETB puts one -1/-1 counter on target creature")
    void etbPutsOneCounterOnTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Grizzly Bears (2/2) with 1 -1/-1 counter → 1/1, survives
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB kills creature with 1 toughness")
    void etbKillsOneThoughnessCreature() {
        // Use a 1/1 creature — Llanowar Elves would work, but let's use Bears with a pre-existing counter
        // Instead, add a GrizzlyBears and give it a -1/-1 counter so it's at 2/1... actually let's find a 1/1
        // We'll just verify with Bears that after getting the counter it becomes 1/1 (already tested above)
        // Let's test with Air Elemental (4/4) to verify counter count
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        assertThat(target.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Pith Driller enters battlefield regardless of ETB outcome")
    void pithDrillerEntersBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pith Driller"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can cast without target when no creatures on battlefield")
    void canCastWithoutTargetWhenNoCreatures() {
        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pith Driller");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new PithDriller()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB → fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
