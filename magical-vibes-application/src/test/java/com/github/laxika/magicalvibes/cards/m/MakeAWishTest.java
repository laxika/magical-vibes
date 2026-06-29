package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MakeAWishTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has spell effect that returns two random cards from graveyard to hand")
    void hasCorrectEffect() {
        MakeAWish card = new MakeAWish();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);

        ReturnCardFromGraveyardEffect effect =
                (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.returnAtRandom()).isTrue();
        assertThat(effect.randomCount()).isEqualTo(2);
        assertThat(effect.filter()).isNull();
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Returns two cards at random from graveyard to hand")
    void returnsTwoCardsFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new MakeAWish()));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Two of the three graveyard cards should be in hand now
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // 1 remaining card + Make a Wish itself goes to graveyard after resolution
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returns only one card when graveyard has only one card")
    void returnsOneCardWhenGraveyardHasOnlyOne() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Only Make a Wish itself in graveyard after resolution
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Make a Wish");
    }

    @Test
    @DisplayName("Returns exactly two when graveyard has exactly two cards")
    void returnsTwoWhenGraveyardHasExactlyTwo() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        // Only Make a Wish itself in graveyard after resolution
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Make a Wish");
    }

    @Test
    @DisplayName("Does nothing when graveyard is empty")
    void doesNothingWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Make a Wish goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new MakeAWish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Make a Wish");
    }
}
