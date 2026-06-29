package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByConvergeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArcaneOmensTest extends BaseCardTest {

    @Test
    @DisplayName("Arcane Omens targets a player and discards by Converge")
    void hasCorrectEffects() {
        ArcaneOmens card = new ArcaneOmens();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(TargetPlayerDiscardsByConvergeEffect.class);
    }

    @Test
    @DisplayName("Casting with one color of mana snapshots Converge 1 on the stack")
    void convergeOneColor() {
        harness.setHand(player1, List.of(new ArcaneOmens()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(1);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting with five colors of mana snapshots Converge 5 on the stack")
    void convergeFiveColors() {
        harness.setHand(player1, List.of(new ArcaneOmens()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("Resolving with Converge 1 discards one card from target player's hand")
    void resolvesDiscardingOneCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel(), new LightningBolt())));
        harness.setHand(player1, List.of(new ArcaneOmens()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Resolving with Converge 5 discards five cards when possible")
    void resolvesDiscardingFiveCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new SerraAngel(), new LightningBolt(),
                new GiantGrowth(), new GrizzlyBears(), new SerraAngel())));
        harness.setHand(player1, List.of(new ArcaneOmens()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        for (int i = 0; i < 5; i++) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Converge 0 discards no cards")
    void convergeZeroDiscardsNothing() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.setHand(player1, List.of(new ArcaneOmens()));
        // Alternate zero-cost path is not used here; cast without paying colored mana is impossible
        // for {4}{B}, so test via direct stack entry with xValue 0 instead
        gd.stack.add(new StackEntry(
                StackEntryType.SORCERY_SPELL,
                new ArcaneOmens(),
                player1.getId(),
                "Arcane Omens",
                List.of(new TargetPlayerDiscardsByConvergeEffect()),
                0,
                player2.getId(),
                null
        ));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
