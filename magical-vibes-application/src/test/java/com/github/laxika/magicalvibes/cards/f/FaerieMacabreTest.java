package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaerieMacabreTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two targeted cards from graveyards and discards the source")
    void exilesTwoTargetsFromGraveyards() {
        harness.setHand(player1, List.of(new FaerieMacabre()));
        Card bears = new GrizzlyBears();
        Card stalwart = new GoldmeadowStalwart();
        Card ownForest = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears, stalwart));
        harness.setGraveyard(player1, List.of(ownForest));

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(bears.getId(), stalwart.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // The untargeted card and the discarded source remain in player1's graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownForest);
        harness.assertInGraveyard(player1, "Faerie Macabre");
    }

    @Test
    @DisplayName("May exile a single card (up to two)")
    void exilesSingleTarget() {
        harness.setHand(player1, List.of(new FaerieMacabre()));
        Card bears = new GrizzlyBears();
        Card stalwart = new GoldmeadowStalwart();
        harness.setGraveyard(player2, List.of(bears, stalwart));

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(stalwart);
    }

    @Test
    @DisplayName("Cannot target more than two cards; the source stays in hand")
    void rejectsMoreThanTwoTargets() {
        harness.setHand(player1, List.of(new FaerieMacabre()));
        Card bears = new GrizzlyBears();
        Card stalwart = new GoldmeadowStalwart();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears, stalwart, third));

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(bears.getId(), stalwart.getId(), third.getId())))
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
