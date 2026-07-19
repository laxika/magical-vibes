package com.github.laxika.magicalvibes.cards.k;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnightOfTheReliquaryTest extends BaseCardTest {

    @Nested
    @DisplayName("Power/Toughness boost")
    class PowerToughnessTests {

        @Test
        @DisplayName("Gets +1/+1 for each land card in your graveyard")
        void boostsPerLandCardInGraveyard() {
            Permanent knight = addKnight(player1);
            int basePower = gqs.getEffectivePower(gd, knight);
            int baseToughness = gqs.getEffectiveToughness(gd, knight);

            harness.setGraveyard(player1, List.of(new Forest(), new Plains(), new Forest()));

            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(basePower + 3);
            assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(baseToughness + 3);
        }

        @Test
        @DisplayName("Does not count non-land cards in your graveyard")
        void ignoresNonLandCardsInGraveyard() {
            Permanent knight = addKnight(player1);
            int basePower = gqs.getEffectivePower(gd, knight);

            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));

            // Only the single land card counts.
            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(basePower + 1);
        }

        @Test
        @DisplayName("Does not count land cards in an opponent's graveyard")
        void ignoresOpponentGraveyardLands() {
            Permanent knight = addKnight(player1);
            int basePower = gqs.getEffectivePower(gd, knight);

            harness.setGraveyard(player2, List.of(new Forest(), new Plains()));

            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(basePower);
        }
    }

    @Nested
    @DisplayName("Search activated ability")
    class SearchAbilityTests {

        @Test
        @DisplayName("Sacrifices a Forest and searches library for a land onto the battlefield")
        void sacrificesForestAndFetchesLand() {
            Permanent knight = addKnight(player1);
            harness.addToBattlefield(player1, new Forest());

            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).addAll(List.of(new Island(), new GrizzlyBears()));

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            // Search prompt only offers land cards.
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                    .allMatch(c -> c.hasType(CardType.LAND));

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            // The Forest was sacrificed to pay the cost, the Island is fetched to the battlefield.
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Forest"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Island"));
        }

        @Test
        @DisplayName("Fetched land enters the battlefield untapped")
        void fetchedLandEntersUntapped() {
            addKnight(player1);
            harness.addToBattlefield(player1, new Plains());

            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).add(new Island());

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            assertThat(findPermanent(player1, "Island").isTapped()).isFalse();
        }

        @Test
        @DisplayName("Cannot activate without a Forest or Plains to sacrifice")
        void cannotActivateWithoutForestOrPlains() {
            addKnight(player1);
            harness.addToBattlefield(player1, new Island()); // a land, but not a Forest or Plains

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ===== Helpers =====

    private Permanent addKnight(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new KnightOfTheReliquary());
        Permanent knight = findPermanent(player, "Knight of the Reliquary");
        knight.setSummoningSick(false);
        return knight;
    }
}
