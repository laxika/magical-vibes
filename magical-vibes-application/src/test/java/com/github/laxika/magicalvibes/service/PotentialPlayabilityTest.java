package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WanderwineHub;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code GameBroadcastService.getPotentialPlayableCardIndices}: hand cards the player could
 * cast if they also tapped their untapped mana sources (MTGO-style click-to-cast), plus
 * {@code getPotentialManaTotal} used by the frontend to cap X.
 */
class PotentialPlayabilityTest extends BaseCardTest {

    private GameBroadcastService broadcast() {
        return harness.getGameBroadcastService();
    }

    private static Card createCreature(String manaCost, CardColor color) {
        Card card = new Card();
        card.setName("Test Creature " + manaCost);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("A card affordable only by tapping lands is potentially playable but not strictly playable")
    void unaffordableCardIsPotentiallyPlayable() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(createCreature("{1}{G}", CardColor.GREEN)));
        harness.ensurePriority(player1);

        List<Integer> strict = broadcast().getPlayableCardIndices(gd, player1.getId());
        assertThat(strict).isEmpty();
        assertThat(broadcast().getPotentialPlayableCardIndices(gd, player1.getId(), strict)).containsExactly(0);
    }

    @Test
    @DisplayName("A card whose colors the lands can't produce is not potentially playable")
    void wrongColorsAreNotPotentiallyPlayable() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(createCreature("{R}", CardColor.RED)));
        harness.ensurePriority(player1);

        List<Integer> strict = broadcast().getPlayableCardIndices(gd, player1.getId());
        assertThat(broadcast().getPotentialPlayableCardIndices(gd, player1.getId(), strict)).isEmpty();
    }

    @Test
    @DisplayName("Strictly playable cards stay in the potential list (floating mana)")
    void strictlyPlayableCardsAreIncluded() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(createCreature("{G}", CardColor.GREEN)));
        harness.ensurePriority(player1);

        List<Integer> strict = broadcast().getPlayableCardIndices(gd, player1.getId());
        assertThat(strict).containsExactly(0);
        assertThat(broadcast().getPotentialPlayableCardIndices(gd, player1.getId(), strict)).containsExactly(0);
    }

    @Test
    @DisplayName("Tapped lands don't count toward potential mana")
    void tappedLandsDontCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);
        harness.tapPermanent(player1, 0);
        // The G from the tap is floating, but the land itself contributes nothing further
        assertThat(broadcast().getPotentialManaTotal(gd, player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A dual land counts once toward the potential total despite producing either color")
    void dualLandCountsOnce() {
        harness.addToBattlefield(player1, new WanderwineHub());
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);

        assertThat(broadcast().getPotentialManaTotal(gd, player1.getId())).isEqualTo(2);
    }
}
