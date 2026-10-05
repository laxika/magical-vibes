package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EmberWeaver;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.ReliquaryTower;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfTheReliquary.class, Forest.class, Plains.class, Island.class,
        EmberWeaver.class, ReliquaryTower.class})
class KnightOfTheReliquaryTest extends BaseCardTest {

    @Nested
    @DisplayName("Power/Toughness boost")
    @CardUsed({KnightOfTheReliquary.class, Forest.class, Plains.class, EmberWeaver.class})
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

            harness.setGraveyard(player1, List.of(new EmberWeaver(), new Forest()));

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

        @Test
        void boostDecreasesWhenLandCardsLeaveGraveyard() {
            Permanent knight = addKnight(player1);
            int initialPower = gqs.getEffectivePower(gd, knight);
            int initialToughness = gqs.getEffectiveToughness(gd, knight);
            harness.setGraveyard(player1, List.of(new Forest(), new Plains()));
            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(initialPower + 2);
            assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(initialToughness + 2);

            harness.setGraveyard(player1, List.of(new Forest()));

            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(initialPower + 1);
            assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(initialToughness + 1);
        }
    }

    @Nested
    @DisplayName("Search activated ability")
    @CardUsed({KnightOfTheReliquary.class, Forest.class, Plains.class, Island.class,
            EmberWeaver.class, ReliquaryTower.class})
    class SearchAbilityTests {

        @Test
        @DisplayName("Sacrifices a Forest and searches library for a land onto the battlefield")
        void sacrificesForestAndFetchesLand() {
            addKnight(player1);
            harness.addToBattlefield(player1, new Forest());

            harness.setLibrary(player1, List.of(new Island(), new EmberWeaver()));

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            // Search prompt only offers land cards.
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                    .allMatch(c -> c.hasType(CardType.LAND));

            harness.handleCardChosen(player1, 0);

            // The Forest was sacrificed to pay the cost, the Island is fetched to the battlefield.
            harness.assertInGraveyard(player1, "Forest");
            harness.assertOnBattlefield(player1, "Island");
        }

        @Test
        @DisplayName("Fetched land enters the battlefield untapped")
        void fetchedLandEntersUntapped() {
            addKnight(player1);
            harness.addToBattlefield(player1, new Plains());

            harness.setLibrary(player1, List.of(new Island()));

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);

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

        @Test
        void sacrificeAndTapArePaidBeforeSearchResolves() {
            Permanent knight = addKnight(player1);
            int initialPower = gqs.getEffectivePower(gd, knight);
            int initialToughness = gqs.getEffectiveToughness(gd, knight);
            Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
            forest.setTapped(true);
            harness.setLibrary(player1, List.of(new ReliquaryTower()));

            harness.activateAbility(player1, 0, null, null);

            assertThat(knight.isTapped()).isTrue();
            harness.assertInGraveyard(player1, "Forest");
            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(initialPower + 1);
            assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(initialToughness + 1);
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);

            harness.assertOnBattlefield(player1, "Reliquary Tower");
            assertThat(findPermanent(player1, "Reliquary Tower").isTapped()).isFalse();
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        }

        @Test
        void mayFailToFindEvenWhenLibraryContainsLand() {
            addKnight(player1);
            harness.addToBattlefield(player1, new Plains());
            harness.setLibrary(player1, List.of(new Island()));

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, -1);

            harness.assertInGraveyard(player1, "Plains");
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(findPermanent(player1, "Knight of the Reliquary"));
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        void cannotActivateWhileSummoningSick() {
            Permanent knight = addKnight(player1);
            knight.setSummoningSick(true);
            harness.addToBattlefield(player1, new Forest());

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
            harness.assertOnBattlefield(player1, "Forest");
        }

        @Test
        void cannotActivateWhileTapped() {
            Permanent knight = addKnight(player1);
            knight.setTapped(true);
            harness.addToBattlefield(player1, new Forest());

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
            harness.assertOnBattlefield(player1, "Forest");
        }

        @Test
        void cannotSacrificeOpponentsForest() {
            addKnight(player1);
            harness.addToBattlefield(player2, new Forest());

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
            harness.assertOnBattlefield(player2, "Forest");
        }

        @Test
        void resolvesWithoutFindingLandWhenLibraryHasNoLands() {
            Permanent knight = addKnight(player1);
            harness.addToBattlefield(player1, new Forest());
            harness.setLibrary(player1, List.of(new EmberWeaver()));

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Forest");
            assertThat(knight.isTapped()).isTrue();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(knight);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }

    private Permanent addKnight(com.github.laxika.magicalvibes.model.Player player) {
        Permanent knight = harness.addToBattlefieldAndReturn(player, new KnightOfTheReliquary());
        knight.setSummoningSick(false);
        return knight;
    }
}
