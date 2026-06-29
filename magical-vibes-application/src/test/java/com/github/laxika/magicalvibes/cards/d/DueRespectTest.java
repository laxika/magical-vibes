package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsEnterTappedThisTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DueRespectTest extends BaseCardTest {

    @Test
    @DisplayName("Due Respect has correct effects")
    void hasCorrectEffects() {
        DueRespect card = new DueRespect();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(PermanentsEnterTappedThisTurnEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Due Respect makes creatures enter tapped this turn")
    void creaturesEnterTappedThisTurn() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        // Now cast a creature — it should enter tapped
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Due Respect makes artifacts enter tapped this turn")
    void artifactsEnterTappedThisTurn() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent ornithopter = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ornithopter"))
                .findFirst()
                .orElseThrow();
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Due Respect makes lands enter tapped this turn")
    void landsEnterTappedThisTurn() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new Forest()));
        gs.playCard(gd, player1, 0, 0, null, null);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Due Respect draws a card")
    void drawsACard() {
        harness.setHand(player1, List.of(new DueRespect()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0);

        // Due Respect was cast from hand (hand -1) then draws a card (+1), net 0
        // But the card was removed from hand when cast, so hand should be handSizeBefore - 1 + 1
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }
}
