package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LichsDuelMastery.class, GrizzlyBears.class})
class LichsDuelMasteryTest extends BaseCardTest {

    private UUID castMastery(int librarySize) {
        harness.setLibrary(player1, java.util.stream.Stream.generate(GrizzlyBears::new)
                .limit(librarySize)
                .map(Card.class::cast)
                .toList());
        harness.castFromHand(player1, new LichsDuelMastery(), "{3}{B}{B}{B}");
        harness.passBothPriorities(); // resolve the enchantment
        harness.passBothPriorities(); // resolve its ETB trigger
        return harness.getPermanentId(player1, "Lich's Duel Mastery");
    }

    @Test
    @DisplayName("Enters with the top five cards of its controller's library as face-down shields")
    void entersWithFiveShields() {
        UUID masteryId = castMastery(6);

        assertThat(gd.getCardsExiledByPermanent(masteryId)).hasSize(5);
        assertThat(gd.exiledCards).filteredOn(e -> masteryId.equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Replaces life loss by returning one shield to its controller's hand")
    void lifeLossReturnsOneShieldToHand() {
        UUID masteryId = castMastery(5);
        List<UUID> shieldIds = gd.getCardsExiledByPermanent(masteryId).stream()
                .map(Card::getId).toList();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 3, "test"));

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsAnyElementsOf(shieldIds);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.getCardsExiledByPermanent(masteryId)).hasSize(4);
    }

    @Test
    @DisplayName("Sacrifices itself when no shields remain")
    void sacrificesWhenNoShieldsRemain() {
        UUID masteryId = castMastery(1);
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 1, "first test"));
        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 1, "second test"));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(masteryId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof LichsDuelMastery);

        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Only the controller's life loss is replaced")
    void onlyControllerLifeLossIsReplaced() {
        UUID masteryId = castMastery(5);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 3, "opponent test"));

        harness.assertLife(player2, 17);
        assertThat(gd.getCardsExiledByPermanent(masteryId)).hasSize(5);
    }
}
