package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HazoretsUndyingFuryTest extends BaseCardTest {

    // ===== Shuffle, exile, and free-cast =====

    @Nested
    @DisplayName("Shuffle, exile the top four, may cast spells with mana value 5 or less")
    class ExileAndCast {

        @Test
        @DisplayName("Exiles exactly the top four cards of the library")
        void exilesTopFourCards() {
            // All lands: nothing castable, so no cast interaction — clean count assertion.
            cast(List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

            assertThat(gd.exiledCards).hasSize(4);
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        }

        @Test
        @DisplayName("Offers only exiled spells with mana value 5 or less; lands and pricier spells stay exiled")
        void onlyOffersSpellsManaValueFiveOrLess() {
            Shock shock = new Shock();               // instant, MV 1
            GrizzlyBears bears = new GrizzlyBears();  // creature, MV 2
            AvatarOfMight avatar = new AvatarOfMight(); // creature, MV 7
            Forest forest = new Forest();            // land, not a spell

            cast(List.of(shock, bears, avatar, forest));

            PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                    (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
            assertThat(interaction.validCardIds()).contains(shock.getId(), bears.getId());
            assertThat(interaction.validCardIds()).doesNotContain(avatar.getId(), forest.getId());
        }

        @Test
        @DisplayName("Choosing an exiled spell casts it without paying its mana cost")
        void castsChosenSpellWithoutPaying() {
            Shock shock = new Shock();
            harness.addToBattlefield(player2, new GrizzlyBears());

            cast(List.of(shock));

            harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
            UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.handlePermanentChosen(player1, bearId);

            assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Shock"))).isTrue();
        }

        @Test
        @DisplayName("With no castable spells among the exiled cards, resolution finishes without a cast choice")
        void noCastableSpellsFinishesWithoutInteraction() {
            cast(List.of(new Forest(), new Forest()));

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.exiledCards).hasSize(2);
        }
    }

    // ===== Lands you control don't untap during your next untap step =====

    @Nested
    @DisplayName("Lands you control don't untap during your next untap step")
    class LandsDontUntap {

        @Test
        @DisplayName("Marks each of the controller's lands to skip their next untap")
        void marksControllerLands() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            plains.tap();

            cast(List.of(new Forest(), new Forest()));

            assertThat(plains.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Land lock is applied even while the free-cast choice is still pending")
        void landLockAppliedWithPendingCastChoice() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            plains.tap();

            cast(List.of(new Shock()));

            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
            assertThat(plains.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Controller's lands stay tapped through their next untap step")
        void landsStayTappedNextUntapStep() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            plains.tap();

            // Six lands so four are exiled and the library still has cards to draw when the turn
            // advances back around to player1 (drawing from an empty library would end the game).
            cast(List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

            // player1's turn -> player2's untap -> player1's next untap
            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(plains.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Opponent's lands are unaffected")
        void opponentLandsUnaffected() {
            Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
            opponentPlains.tap();

            cast(List.of(new Forest(), new Forest()));

            assertThat(opponentPlains.getSkipUntapCount()).isZero();
        }
    }

    // ===== Helpers =====

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new HazoretsUndyingFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
