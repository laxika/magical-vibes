package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolemnSimulacrum.class, Forest.class, Island.class, Plains.class,
        AncientDen.class, CopperMyr.class, Shatter.class})
class SolemnSimulacrumTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB land search")
    @CardUsed({SolemnSimulacrum.class, Forest.class, Island.class, Plains.class,
            AncientDen.class, CopperMyr.class})
    class EnterTheBattlefield {

        @Test
        @DisplayName("Accepting the may ability presents only basic land cards from the library")
        void acceptingPresentsBasicLands() {
            castSolemnSimulacrum();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player1.getId());

            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                    .hasSize(3)
                    .allMatch(c -> c.hasType(CardType.LAND)
                            && c.getSupertypes().contains(CardSupertype.BASIC));
        }

        @Test
        @DisplayName("Chosen basic land enters the battlefield tapped")
        void chosenLandEntersTapped() {
            castSolemnSimulacrum();
            setupLibrary();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.handleCardChosen(player1, 0);

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            assertThat(battlefield).anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
            assertThat(battlefield.stream().filter(p -> p.getCard().hasType(CardType.LAND)).count()).isEqualTo(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Declining the may ability skips the library search")
        void decliningSkipsSearch() {
            castSolemnSimulacrum();
            setupLibrary();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        }

        @Test
        @DisplayName("Player may fail to find a basic land")
        void mayFailToFind() {
            castSolemnSimulacrum();
            setupLibrary();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.handleCardChosen(player1, -1);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().hasType(CardType.LAND));
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("No search prompt when the library holds no basic lands")
        void noBasicLandsInLibrary() {
            castSolemnSimulacrum();
            harness.setLibrary(player1, List.of(new CopperMyr(), new CopperMyr()));

            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        }

        @Test
        @DisplayName("Accepting the search with an empty library finishes without putting a land onto the battlefield")
        void emptyLibrarySearchCompletes() {
            castSolemnSimulacrum();
            harness.setLibrary(player1, List.of());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().hasType(CardType.LAND));
            harness.assertOnBattlefield(player1, "Solemn Simulacrum");
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("Death draw")
    @CardUsed({SolemnSimulacrum.class, Shatter.class, Forest.class})
    class Death {

        @Test
        @DisplayName("Accepting the death may ability draws a card")
        void deathAcceptDraws() {
            harness.addToBattlefield(player1, new SolemnSimulacrum());
            harness.setHand(player1, List.of(new Shatter()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            int handBefore = gd.playerHands.get(player1.getId()).size();

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Solemn Simulacrum"));

            harness.assertInGraveyard(player1, "Solemn Simulacrum");

            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 1);
        }

        @Test
        @DisplayName("Declining the death may ability draws nothing")
        void deathDeclineDrawsNothing() {
            harness.addToBattlefield(player1, new SolemnSimulacrum());
            harness.setHand(player1, List.of(new Shatter()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            int handBefore = gd.playerHands.get(player1.getId()).size();

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Solemn Simulacrum"));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
        }

        @Test
        @DisplayName("Death trigger draws for the dying creature's controller even after its card leaves the graveyard")
        void deathDrawUsesControllerAndSurvivesSourceLeavingGraveyard() {
            SolemnSimulacrum solemn = new SolemnSimulacrum();
            harness.addToBattlefield(player2, solemn);
            harness.setHand(player2, List.of());
            harness.setLibrary(player2, List.of(new Forest()));
            harness.setHand(player1, List.of(new Shatter()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castAndResolveInstant(player1, 0,
                    harness.getPermanentId(player2, "Solemn Simulacrum"));
            harness.assertInGraveyard(player2, "Solemn Simulacrum");
            harness.setGraveyard(player2, List.of());
            harness.setExile(player2, List.of(solemn));

            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player2.getId());
            harness.handleMayAbilityChosen(player2, true);

            harness.assertInHand(player2, "Forest");
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
        }
    }

    private void castSolemnSimulacrum() {
        harness.castFromHand(player1, new SolemnSimulacrum(), "{4}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains(), new AncientDen()));
    }
}
