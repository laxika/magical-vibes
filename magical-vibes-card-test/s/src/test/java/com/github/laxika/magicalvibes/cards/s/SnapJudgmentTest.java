package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnapJudgment.class, GrizzlyBears.class})
class SnapJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards Snap Judgment and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new SnapJudgment()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snap Judgment");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling pays one generic mana and discards before drawing")
    void cyclingPaysCostsBeforeResolution() {
        SnapJudgment drawnCard = new SnapJudgment();
        harness.setHand(player1, List.of(new SnapJudgment()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Snap Judgment");
        harness.assertInGraveyard(player1, "Snap Judgment");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated without mana and does not discard")
    void cyclingRequiresMana() {
        harness.setHand(player1, List.of(new SnapJudgment()));
        harness.setLibrary(player1, List.of(new SnapJudgment()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Snap Judgment");
        harness.assertNotInGraveyard(player1, "Snap Judgment");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
