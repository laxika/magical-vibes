package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.f.FranticSalvage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GruesomeEncore;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MorbidPlunder;
import com.github.laxika.magicalvibes.cards.n.NihilSpellbomb;
import com.github.laxika.magicalvibes.cards.r.Recover;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GraveyardReturnResolutionServiceTest extends BaseCardTest {

    // =========================================================================
    // describeFilter — static utility method
    // =========================================================================

    @Nested
    @DisplayName("describeFilter")
    class DescribeFilterTests {

        @Test
        @DisplayName("null predicate returns 'card'")
        void nullPredicateReturnsCard() {
            assertThat(CardPredicateUtils.describeFilter(null)).isEqualTo("card");
        }

        @Test
        @DisplayName("CardTypePredicate(CREATURE) returns 'creature card'")
        void cardTypePredicateCreature() {
            CardPredicate predicate = new CardTypePredicate(CardType.CREATURE);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("creature card");
        }

        @Test
        @DisplayName("CardTypePredicate(ARTIFACT) returns 'artifact card'")
        void cardTypePredicateArtifact() {
            CardPredicate predicate = new CardTypePredicate(CardType.ARTIFACT);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("artifact card");
        }

        @Test
        @DisplayName("CardSubtypePredicate returns subtype display name + 'card'")
        void cardSubtypePredicate() {
            CardPredicate predicate = new CardSubtypePredicate(CardSubtype.ZOMBIE);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("Zombie card");
        }

        @Test
        @DisplayName("CardKeywordPredicate returns 'card with <keyword>'")
        void cardKeywordPredicate() {
            CardPredicate predicate = new CardKeywordPredicate(Keyword.INFECT);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("card with infect");
        }

        @Test
        @DisplayName("CardKeywordPredicate with multi-word keyword uses spaces")
        void cardKeywordPredicateMultiWord() {
            CardPredicate predicate = new CardKeywordPredicate(Keyword.FIRST_STRIKE);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("card with first strike");
        }

        @Test
        @DisplayName("CardIsAuraPredicate returns 'Aura card'")
        void cardIsAuraPredicate() {
            CardPredicate predicate = new CardIsAuraPredicate();
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("Aura card");
        }

        @Test
        @DisplayName("CardAllOfPredicate combines descriptions merging 'card with' parts")
        void cardAllOfPredicateMerges() {
            CardPredicate predicate = new CardAllOfPredicate(List.of(
                    new CardTypePredicate(CardType.CREATURE),
                    new CardKeywordPredicate(Keyword.INFECT)
            ));
            assertThat(CardPredicateUtils.describeFilter(predicate))
                    .isEqualTo("creature card with infect");
        }

        @Test
        @DisplayName("CardAnyOfPredicate with same suffix merges to 'X or Y card'")
        void cardAnyOfPredicateMergesSuffix() {
            CardPredicate predicate = new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.ARTIFACT),
                    new CardTypePredicate(CardType.CREATURE)
            ));
            assertThat(CardPredicateUtils.describeFilter(predicate))
                    .isEqualTo("artifact or creature card");
        }

        @Test
        @DisplayName("CardAnyOfPredicate with mixed predicates joins with 'or'")
        void cardAnyOfPredicateMixed() {
            CardPredicate predicate = new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.CREATURE),
                    new CardIsAuraPredicate()
            ));
            assertThat(CardPredicateUtils.describeFilter(predicate))
                    .isEqualTo("creature or Aura card");
        }
    }

    // =========================================================================
    // ReturnTargetCardsFromGraveyardToHandEffect (via Morbid Plunder)
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnTargetCardsFromGraveyardToHand")
    class ReturnTargetCardsToHandTests {

        @Test
        @DisplayName("Returns multiple targeted cards from graveyard to hand")
        void returnsMultipleTargetedCardsToHand() {
            Card creature1 = new GrizzlyBears();
            Card creature2 = new LlanowarElves();
            harness.setGraveyard(player1, List.of(creature1, creature2));
            harness.setHand(player1, List.of(new MorbidPlunder()));
            harness.addMana(player1, ManaColor.BLACK, 3);

            harness.castSorcery(player1, 0, 0);
            List<UUID> validIds = new ArrayList<>(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds());
            harness.handleMultipleGraveyardCardsChosen(player1, validIds);
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Llanowar Elves");
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertInHand(player1, "Llanowar Elves");
        }

        @Test
        @DisplayName("Does nothing when no targets are selected")
        void doesNothingWhenNoTargets() {
            harness.setGraveyard(player1, List.of(new GrizzlyBears()));
            harness.setHand(player1, List.of(new MorbidPlunder()));
            harness.addMana(player1, ManaColor.BLACK, 3);

            harness.castSorcery(player1, 0, 0);
            harness.handleMultipleGraveyardCardsChosen(player1, List.of());
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Grizzly Bears");
            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }
    }

    // =========================================================================
    // PutTargetCardsFromGraveyardOnTopOfLibraryEffect (via Frantic Salvage)
    // =========================================================================

    @Nested
    @DisplayName("resolvePutTargetCardsFromGraveyardOnTopOfLibrary")
    class PutCardsOnTopOfLibraryTests {

        @Test
        @DisplayName("Moves targeted artifact cards from graveyard to top of library")
        void movesArtifactCardsToTopOfLibrary() {
            Card artifact1 = new LeoninScimitar();
            Card artifact2 = new RodOfRuin();
            harness.setGraveyard(player1, List.of(artifact1, artifact2));
            harness.setHand(player1, List.of(new FranticSalvage()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            // Frantic Salvage is an instant — cast it
            harness.castInstant(player1, 0);

            // Should prompt for multi-graveyard choice (artifact cards)
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
            List<UUID> validIds = new ArrayList<>(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds());
            harness.handleMultipleGraveyardCardsChosen(player1, validIds);
            harness.passBothPriorities();

            // Artifacts should no longer be in graveyard
            harness.assertNotInGraveyard(player1, "Leonin Scimitar");
            harness.assertNotInGraveyard(player1, "Rod of Ruin");

            // Both artifacts were put on top of library, then DrawCardEffect drew the topmost one.
            // So one should be in library and one in hand.
            List<Card> library = gd.playerDecks.get(player1.getId());
            List<Card> hand = gd.playerHands.get(player1.getId());
            boolean scimitarFound = library.stream().anyMatch(c -> c.getName().equals("Leonin Scimitar"))
                    || hand.stream().anyMatch(c -> c.getName().equals("Leonin Scimitar"));
            boolean rodFound = library.stream().anyMatch(c -> c.getName().equals("Rod of Ruin"))
                    || hand.stream().anyMatch(c -> c.getName().equals("Rod of Ruin"));
            assertThat(scimitarFound).isTrue();
            assertThat(rodFound).isTrue();
        }

        @Test
        @DisplayName("Non-artifact cards are not valid targets for artifact filter")
        void nonArtifactNotValidTarget() {
            Card creature = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(creature));
            harness.setHand(player1, List.of(new FranticSalvage()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            // Frantic Salvage is an instant
            harness.castInstant(player1, 0);

            // No valid artifact targets, so no graveyard choice prompt
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        }
    }

    // =========================================================================
    // PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect
    //   (via Gruesome Encore)
    // =========================================================================

    @Nested
    @DisplayName("resolvePutCreatureFromOpponentGraveyardWithExile")
    class PutFromOpponentGraveyardWithExileTests {

        @Test
        @DisplayName("Puts creature from opponent's graveyard onto battlefield with haste")
        void putsCreatureWithHaste() {
            Card target = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(target));
            harness.setHand(player1, List.of(new GruesomeEncore()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, target.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player2, "Grizzly Bears");

            Permanent creature = findPermanent(player1, "Grizzly Bears");
            assertThat(creature.getGrantedKeywords()).contains(Keyword.HASTE);
            assertThat(creature.isExileIfLeavesBattlefield()).isTrue();
            assertThat(gd.pendingTokenExilesAtEndStep).contains(creature.getId());
            assertThat(gd.stolenCreatures).containsKey(creature.getId());
        }

        @Test
        @DisplayName("Fizzles if target card is no longer in graveyard")
        void fizzlesIfTargetGone() {
            Card target = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(target));
            harness.setHand(player1, List.of(new GruesomeEncore()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, target.getId());
            // Remove target before resolution
            gd.playerGraveyards.get(player2.getId()).clear();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }
    }

    // =========================================================================
    // ExileTargetPlayerGraveyardEffect (via Nihil Spellbomb)
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPlayerGraveyard")
    class ExileTargetPlayerGraveyardTests {

        @Test
        @DisplayName("Exiles all cards from target player's graveyard")
        void exilesEntireGraveyard() {
            Card creature = new GrizzlyBears();
            Card artifact = new LeoninScimitar();
            harness.setGraveyard(player2, List.of(creature, artifact));

            // Add Nihil Spellbomb to battlefield (not summoning sick — artifacts don't have it)
            harness.addToBattlefield(player1, new NihilSpellbomb());

            // Activate: {T}, Sacrifice Nihil Spellbomb: Exile target player's graveyard
            harness.activateAbility(player1, 0, null, player2.getId());

            // Sacrificing triggers ON_DEATH may-ability (Pay {B} to draw a card?) — decline it
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            harness.handleMayAbilityChosen(player1, false);

            harness.passBothPriorities();

            // Player2's graveyard should be empty
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
            // Cards should be in exile
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                    .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
        }

        @Test
        @DisplayName("Logs message when targeting empty graveyard")
        void logsMessageForEmptyGraveyard() {
            harness.setGraveyard(player2, List.of());
            harness.addToBattlefield(player1, new NihilSpellbomb());

            harness.activateAbility(player1, 0, null, player2.getId());

            // Sacrificing triggers ON_DEATH may-ability — decline it
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            harness.handleMayAbilityChosen(player1, false);

            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("already empty"));
        }
    }

    // =========================================================================
    // ReturnCardFromGraveyardEffect — pre-targeted to hand (via Recover)
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnCardFromGraveyard — pre-targeted")
    class PreTargetedReturnTests {

        @Test
        @DisplayName("Returns pre-targeted creature card from graveyard to hand")
        void returnsPreTargetedCreatureToHand() {
            Card creature = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(creature));
            harness.setHand(player1, List.of(new Recover()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castSorcery(player1, 0, creature.getId());
            harness.passBothPriorities();

            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Fizzles when pre-targeted card is no longer in graveyard")
        void fizzlesWhenTargetGone() {
            Card creature = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(creature));
            harness.setHand(player1, List.of(new Recover()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castSorcery(player1, 0, creature.getId());
            // Remove target before resolution
            gd.playerGraveyards.get(player1.getId()).clear();
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }
    }

    // =========================================================================
    // ReturnCardFromGraveyardEffect — search all graveyards (via Beacon of Unrest)
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnCardFromGraveyard — all graveyards search")
    class AllGraveyardsSearchTests {

        @Test
        @DisplayName("Prompts graveyard choice when matching cards exist in any graveyard")
        void promptsChoiceWhenMatchingCardsExist() {
            Card creature = new GrizzlyBears();
            Card artifact = new LeoninScimitar();
            harness.setGraveyard(player1, List.of(creature));
            harness.setGraveyard(player2, List.of(artifact));
            harness.setHand(player1, List.of(new BeaconOfUnrest()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            // Should be awaiting graveyard choice (search all graveyards)
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Puts chosen creature from opponent's graveyard onto battlefield")
        void putsChosenCardOntoBattlefield() {
            Card creature = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(creature));
            harness.setHand(player1, List.of(new BeaconOfUnrest()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            // Choose the creature (index 0 in the card pool)
            harness.handleGraveyardCardChosen(player1, 0);

            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Logs message and skips shuffle when no matching cards in any graveyard")
        void logsMessageWhenNoMatchingCards() {
            // No artifact or creature cards in any graveyard
            harness.setGraveyard(player1, List.of());
            harness.setGraveyard(player2, List.of());
            harness.setHand(player1, List.of(new BeaconOfUnrest()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            // Should not be awaiting input (no valid choices)
            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("no ") && log.contains("in any graveyard"));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(cardName + " not found on battlefield"));
    }
}
