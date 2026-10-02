package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaerieMacabre.class, Cinderbones.class})
class FaerieMacabreTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two targeted cards from graveyards and discards the source")
    void exilesTwoTargetsFromGraveyards() {
        harness.setHand(player1, List.of(new FaerieMacabre()));
        Card first = new Cinderbones();
        Card second = new Cinderbones();
        Card untargeted = new Cinderbones();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setGraveyard(player1, List.of(untargeted));

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // The untargeted card and the discarded source remain in player1's graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(untargeted);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.assertInGraveyard(player1, "Faerie Macabre");
    }

    @Test
    @DisplayName("May exile a single card (up to two)")
    void exilesSingleTarget() {
        harness.setHand(player1, List.of(new FaerieMacabre()));
        Card target = new Cinderbones();
        Card remaining = new Cinderbones();
        harness.setGraveyard(player2, List.of(target, remaining));

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactly(target.getId());
    }

    @Test
    @DisplayName("May target cards in different graveyards")
    void exilesTargetsFromDifferentGraveyards() {
        FaerieMacabre source = new FaerieMacabre();
        harness.setHand(player1, List.of(source));
        Card opponentCard = new Cinderbones();
        Card ownCard = new Cinderbones();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setGraveyard(player1, List.of(ownCard));

        harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(opponentCard.getId(), ownCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player2.getId())).isNotNull().isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(opponentCard.getId(), ownCard.getId());
        harness.assertInGraveyard(player1, "Faerie Macabre");
    }

    @Test
    @DisplayName("May activate with no targets because the ability says up to two")
    void mayChooseNoTargets() {
        harness.setHand(player1, List.of(new FaerieMacabre()));

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Faerie Macabre");
    }

    @Test
    @DisplayName("Cannot target more than two cards; the source stays in hand")
    void rejectsMoreThanTwoTargets() {
        harness.setHand(player1, List.of(new FaerieMacabre()));
        Card first = new Cinderbones();
        Card second = new Cinderbones();
        Card third = new Cinderbones();
        harness.setGraveyard(player2, List.of(first, second, third));

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Faerie Macabre");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects a target that is not in any graveyard; the source stays in hand")
    void rejectsTargetNotInGraveyard() {
        harness.setHand(player1, List.of(new FaerieMacabre()));

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Faerie Macabre");
        assertThat(gd.stack).isEmpty();
    }
}
