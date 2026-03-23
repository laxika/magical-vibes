package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrimAfflictionTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has put -1/-1 counter and proliferate effects")
    void hasCorrectEffects() {
        GrimAffliction card = new GrimAffliction();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(PutMinusOneMinusOneCounterOnTargetCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ProliferateEffect.class);
    }

    // ===== -1/-1 counter + Proliferate =====

    @Test
    @DisplayName("Puts -1/-1 counter on target creature and then proliferates")
    void putsCounterAndProliferates() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Target creature got a -1/-1 counter (now 1/1)
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(target.getMinusOneMinusOneCounters()).isEqualTo(1);

        // Proliferate choice is now awaited — choose both permanents with counters
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId(), otherBears.getId()));

        assertThat(target.getMinusOneMinusOneCounters()).isEqualTo(2);
        assertThat(otherBears.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts counter and proliferates choosing none")
    void putsCounterAndProliferatesNone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Target creature got a -1/-1 counter
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(target.getMinusOneMinusOneCounters()).isEqualTo(1);

        // Proliferate — choose none
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(target.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, targetId);

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no proliferate either
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(otherBears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target non-creature permanents")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.s.Spellbook());
        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Proliferate — choose the target (it has a counter now)
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grim Affliction"));
    }
}
