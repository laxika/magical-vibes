package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Waylay")
@CardUsed(Waylay.class)
class WaylayTest extends BaseCardTest {

    private void castWaylay() {
        harness.castFromHand(player1, new Waylay(), "{2}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creates three 2/2 white Knight tokens")
    void createsKnightTokens() {
        castWaylay();

        List<Permanent> knights = findPermanents(player1, "Knight");
        assertThat(knights).hasSize(3);
        assertThat(knights).allSatisfy(knight -> {
            assertThat(knight.getCard().getPower()).isEqualTo(2);
            assertThat(knight.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Creates white creature tokens with the Knight subtype")
    void createsWhiteKnightCreatureTokens() {
        castWaylay();

        assertThat(findPermanents(player1, "Knight")).allSatisfy(knight -> {
            assertThat(knight.getCard().isToken()).isTrue();
            assertThat(knight.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(gqs.getEffectiveColors(gd, knight)).containsExactly(CardColor.WHITE);
            assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        });
    }

    @Test
    @CardUsed(ClawsOfGix.class)
    @DisplayName("Leaves the tokens available to respond before the cleanup exile resolves")
    void cleanupExileWaitsForPriority() {
        harness.addToBattlefield(player1, new ClawsOfGix());
        harness.setLife(player1, 20);
        castWaylay();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        List<Permanent> knights = findPermanents(player1, "Knight");
        assertThat(knights).hasSize(3);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, knights.getFirst().getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(2);
        harness.assertLife(player1, 21);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    @Test
    @DisplayName("Exiles the tokens at the beginning of the next cleanup step")
    void exilesTokensAtNextCleanup() {
        castWaylay();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Knight")).hasSize(3);

        harness.passUntil(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()).stream()
                .filter(Card::isToken)
                .count()).isZero();
    }
}
