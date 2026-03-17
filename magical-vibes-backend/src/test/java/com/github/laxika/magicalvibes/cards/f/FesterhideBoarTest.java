package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FesterhideBoarTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has MorbidConditionalEffect wrapping PutCountersOnSourceEffect in ON_ENTER_BATTLEFIELD")
    void hasCorrectStructure() {
        FesterhideBoar card = new FesterhideBoar();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MorbidConditionalEffect.class);

        MorbidConditionalEffect morbid =
                (MorbidConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(morbid.wrapped()).isInstanceOf(PutCountersOnSourceEffect.class);

        PutCountersOnSourceEffect counters = (PutCountersOnSourceEffect) morbid.wrapped();
        assertThat(counters.powerModifier()).isEqualTo(1);
        assertThat(counters.toughnessModifier()).isEqualTo(1);
        assertThat(counters.amount()).isEqualTo(2);
    }

    // ===== Without morbid =====

    @Test
    @DisplayName("Enters as a 3/3 without morbid (no counters)")
    void entersWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FesterhideBoar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        GameData gd = harness.getGameData();
        Permanent boar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Festerhide Boar"))
                .findFirst().orElseThrow();

        // No +1/+1 counters without morbid
        assertThat(boar.getPlusOnePlusOneCounters()).isEqualTo(0);
        // Stack should be empty (no ETB trigger placed since morbid not met)
        assertThat(gd.stack).isEmpty();
    }

    // ===== With morbid =====

    @Test
    @DisplayName("Enters with two +1/+1 counters when morbid is met (effectively 5/5)")
    void entersWithMorbidCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FesterhideBoar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Simulate a creature having died this turn
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (enters battlefield with ETB trigger)
        harness.passBothPriorities(); // resolve ETB effect (puts +1/+1 counters)

        GameData gd = harness.getGameData();
        Permanent boar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Festerhide Boar"))
                .findFirst().orElseThrow();

        assertThat(boar.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(boar.getEffectivePower()).isEqualTo(5);
        assertThat(boar.getEffectiveToughness()).isEqualTo(5);
    }

    // ===== Integration: actual creature death enables morbid =====

    @Test
    @DisplayName("Killing a creature with Shock enables morbid for Festerhide Boar")
    void actualCreatureDeathEnablesMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new FesterhideBoar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Cast Shock targeting Grizzly Bears (2 damage kills a 2/2)
        java.util.UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        // Bears should be dead
        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Now cast Festerhide Boar — morbid should be active
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect (puts +1/+1 counters)

        Permanent boar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Festerhide Boar"))
                .findFirst().orElseThrow();

        assertThat(boar.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(boar.getEffectivePower()).isEqualTo(5);
        assertThat(boar.getEffectiveToughness()).isEqualTo(5);
    }
}
