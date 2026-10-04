package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.c.CollectiveBrutality;
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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HazoretsUndyingFury.class, AvatarOfMight.class, CollectiveBrutality.class,
        Forest.class, GrizzlyBears.class, HollowOne.class, Plains.class, Shock.class})
class HazoretsUndyingFuryTest extends BaseCardTest {

    @Nested
    @CardUsed({HazoretsUndyingFury.class, AvatarOfMight.class, CollectiveBrutality.class,
            Forest.class, GrizzlyBears.class, HollowOne.class, Shock.class})
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
            AvatarOfMight avatar = new AvatarOfMight(); // creature, MV 8
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
        @DisplayName("Chooses a targeted modal spell mode before casting it for free")
        void choosesModalSpellModeAndTargetBeforeFreeCast() {
            CollectiveBrutality brutality = new CollectiveBrutality();
            harness.setHand(player2, List.of(new Shock()));

            cast(List.of(brutality));

            harness.handleMultipleCardsChosen(player1, List.of(brutality.getId()));
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

            harness.handleListChoice(player1,
                    "Target opponent reveals their hand. You choose an instant or sorcery card from it. "
                            + "That player discards that card");
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
            harness.handleCardChosen(player1, 0);

            harness.assertInGraveyard(player2, "Shock");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("With no castable spells among the exiled cards, resolution finishes without a cast choice")
        void noCastableSpellsFinishesWithoutInteraction() {
            cast(List.of(new Forest(), new Forest()));

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.exiledCards).hasSize(2);
        }

        @Test
        void mayDeclineAllSpellsAndLeaveThemExiled() {
            HollowOne hollowOne = new HollowOne();
            cast(List.of(hollowOne));

            harness.handleMultipleCardsChosen(player1, List.of());

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.findExiledCard(hollowOne.getId())).isNotNull();
            harness.assertNotOnBattlefield(player1, "Hollow One");
        }

        @Test
        void castsMultipleManaValueFiveSpellsWithoutATotalManaValueLimit() {
            HollowOne first = new HollowOne();
            HollowOne second = new HollowOne();
            cast(List.of(first, second));

            harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(countPermanents(player1, "Hollow One")).isEqualTo(2);
            assertThat(gd.findExiledCard(first.getId())).isNull();
            assertThat(gd.findExiledCard(second.getId())).isNull();
        }

        @Test
        void mayPayEscalateToChooseAnotherModeOfAFreeSpell() {
            CollectiveBrutality brutality = new CollectiveBrutality();
            harness.setHand(player2, List.of(new Shock()));
            cast(List.of(brutality));
            harness.setHand(player1, List.of(new Forest()));

            harness.handleMultipleCardsChosen(player1, List.of(brutality.getId()));
            harness.handleListChoice(player1,
                    "Target opponent reveals their hand. You choose an instant or sorcery card from it. "
                            + "That player discards that card");

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
            PendingInteraction.ColorChoice modes =
                    (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
            assertThat(modes.options()).contains("Target opponent loses 2 life and you gain 2 life");
        }
    }

    @Nested
    @CardUsed({HazoretsUndyingFury.class, Forest.class, Plains.class, Shock.class})
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

        @Test
        void landsEnteringAfterResolutionAlsoStayTapped() {
            cast(List.of(new Forest()));
            Permanent laterLand = harness.addToBattlefieldAndReturn(player1, new Plains());
            laterLand.tap();

            harness.performUntapStep(player2);
            harness.performUntapStep(player1);

            assertThat(laterLand.isTapped()).isTrue();
        }

        @Test
        void landGivenToOpponentMayUntapDuringOpponentsStep() {
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
            land.tap();
            cast(List.of(new Forest()));
            gd.playerBattlefields.get(player1.getId()).remove(land);
            gd.playerBattlefields.get(player2.getId()).add(land);

            harness.performUntapStep(player2);

            assertThat(land.isTapped()).isFalse();
        }

        @Test
        void untapRestrictionExpiresAfterOneControllerUntapStep() {
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
            land.tap();
            cast(List.of(new Forest()));

            harness.performUntapStep(player1);
            assertThat(land.isTapped()).isTrue();
            harness.performUntapStep(player1);
            assertThat(land.isTapped()).isFalse();
        }
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new HazoretsUndyingFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player nextPlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(nextPlayer, TurnStep.UPKEEP);
    }
}
