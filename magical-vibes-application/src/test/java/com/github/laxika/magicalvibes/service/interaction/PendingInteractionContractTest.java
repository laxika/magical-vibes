package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.InteractionOptions;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingCapriciousEfreetState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingKarnRestart;
import com.github.laxika.magicalvibes.model.PendingKarnScionExileReturn;
import com.github.laxika.magicalvibes.model.PendingKarnScionRevealChoice;
import com.github.laxika.magicalvibes.model.PendingKnowledgePoolCast;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.PendingReturnExiledWithSourceCard;
import com.github.laxika.magicalvibes.model.PendingSphinxAmbassadorChoice;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the {@link PendingInteraction} record contract: every kind that can become the
 * active interaction must carry its own deciding player and legal answer space
 * ({@link PendingInteraction#decidingPlayerId()} / {@link PendingInteraction#legalOptions()}),
 * since the interaction handlers, the registry, and the AI simulator all read them off the
 * record. Queue-only carrier records (never the active interaction) are exempt.
 */
class PendingInteractionContractTest {

    /** Records that only wait in the queue and are serviced by beginning a promptable kind. */
    private static final Set<Class<?>> QUEUE_ONLY_CARRIERS = Set.of(
            PermanentChoiceContext.class,
            PendingSphinxAmbassadorChoice.class,
            PendingCapriciousEfreetState.class,
            PendingKarnScionRevealChoice.class,
            PendingKarnScionExileReturn.class,
            PendingReturnExiledWithSourceCard.class,
            PendingKarnRestart.class,
            PendingKnowledgePoolCast.class,
            PendingPileSeparation.class);

    @Test
    @DisplayName("every promptable interaction kind overrides decidingPlayerId() and legalOptions()")
    void everyPromptableKindOverridesTheContractMethods() throws Exception {
        for (Class<?> kind : PendingInteraction.class.getPermittedSubclasses()) {
            if (QUEUE_ONLY_CARRIERS.contains(kind)) {
                continue;
            }
            assertThat(kind.getMethod("decidingPlayerId").getDeclaringClass())
                    .as("%s must carry its deciding player", kind.getSimpleName())
                    .isNotEqualTo(PendingInteraction.class);
            assertThat(kind.getMethod("legalOptions").getDeclaringClass())
                    .as("%s must carry its legal options", kind.getSimpleName())
                    .isNotEqualTo(PendingInteraction.class);
        }
    }

    @Nested
    @DisplayName("decidingPlayerId")
    class DecidingPlayerId {

        @Test
        @DisplayName("kinds whose decider is not the plain playerId component report the right player")
        void divergentDeciders() {
            UUID chooser = UUID.randomUUID();
            UUID target = UUID.randomUUID();

            PendingInteraction revealedHand = new PendingInteraction.RevealedHandChoice(
                    chooser, target, List.of(0), 1, true, false, List.of(), null, "p", false, false);
            assertThat(revealedHand.decidingPlayerId()).isEqualTo(chooser);

            PendingInteraction revealDiscard = new PendingInteraction.RevealCardsDiscardChoice(
                    chooser, target, chooser, false, List.of(0), 1, List.of(), "p", 1);
            assertThat(revealDiscard.decidingPlayerId()).isEqualTo(chooser);

            PendingInteraction attackers = new PendingInteraction.AttackerDeclaration(chooser);
            assertThat(attackers.decidingPlayerId()).isEqualTo(chooser);

            PendingInteraction blockers = new PendingInteraction.BlockerDeclaration(target);
            assertThat(blockers.decidingPlayerId()).isEqualTo(target);

            PendingInteraction search = new PendingInteraction.LibrarySearch(
                    LibrarySearchParams.builder(chooser, List.of()).canFailToFind(true).build(),
                    "p", true);
            assertThat(search.decidingPlayerId()).isEqualTo(chooser);
        }
    }

    @Nested
    @DisplayName("legalOptions")
    class LegalOptions {

        private final UUID playerId = UUID.randomUUID();

        @Test
        @DisplayName("hand-card choices are declinable index picks, discards are not")
        void handChoiceDeclinability() {
            PendingInteraction handChoice = new PendingInteraction.HandCardChoice(playerId, List.of(0, 2), "p");
            assertThat(handChoice.legalOptions())
                    .isEqualTo(new InteractionOptions.CardIndexPick(List.of(0, 2), true));

            PendingInteraction discard = new PendingInteraction.DiscardChoice(playerId, List.of(1), 1, null, "p");
            assertThat(discard.legalOptions())
                    .isEqualTo(new InteractionOptions.CardIndexPick(List.of(1), false));
        }

        @Test
        @DisplayName("graveyard choice declinability mirrors the answer handler's decline rule")
        void graveyardChoiceDeclinability() {
            PendingInteraction toHand = PendingInteraction.GraveyardChoice
                    .builder(playerId, List.of(0), GraveyardChoiceDestination.HAND, "p").build();
            assertThat(toHand.legalOptions())
                    .isEqualTo(new InteractionOptions.GraveyardIndexPick(List.of(0), true));

            PendingInteraction exile = PendingInteraction.GraveyardChoice
                    .builder(playerId, List.of(0), GraveyardChoiceDestination.EXILE, "p").build();
            assertThat(exile.legalOptions())
                    .isEqualTo(new InteractionOptions.GraveyardIndexPick(List.of(0), false));

            PendingInteraction mandatory = PendingInteraction.GraveyardChoice
                    .builder(playerId, List.of(0), GraveyardChoiceDestination.HAND, "p")
                    .mandatory(true).build();
            assertThat(mandatory.legalOptions())
                    .isEqualTo(new InteractionOptions.GraveyardIndexPick(List.of(0), false));
        }

        @Test
        @DisplayName("single- and multi-pick shapes carry the record's own id lists and bounds")
        void pickShapes() {
            UUID permA = UUID.randomUUID();
            UUID permB = UUID.randomUUID();
            UUID targetPlayer = UUID.randomUUID();

            PendingInteraction permanentChoice = new PendingInteraction.PermanentChoice(
                    playerId, List.of(permA), List.of(targetPlayer), null, "p");
            assertThat(permanentChoice.legalOptions())
                    .isEqualTo(new InteractionOptions.PermanentPick(List.of(permA, targetPlayer)));

            PendingInteraction multiPermanent = new PendingInteraction.MultiPermanentChoice(
                    playerId, List.of(permA, permB), 1, null, "p");
            assertThat(multiPermanent.legalOptions())
                    .isEqualTo(new InteractionOptions.MultiPermanentPick(List.of(permA, permB), 0, 1));

            PendingInteraction xValue = new PendingInteraction.XValueChoice(playerId, 4, "p", "card");
            assertThat(xValue.legalOptions()).isEqualTo(new InteractionOptions.NumberPick(0, 4));

            PendingInteraction colorChoice = new PendingInteraction.ColorChoice(
                    playerId, null, null, null, List.of("WHITE", "BLUE"), "p");
            assertThat(colorChoice.legalOptions())
                    .isEqualTo(new InteractionOptions.ListPick(List.of("WHITE", "BLUE")));

            PendingInteraction mayAbility = new PendingInteraction.MayAbilityChoice(playerId, "d", null);
            assertThat(mayAbility.legalOptions()).isEqualTo(InteractionOptions.ACCEPT_DECLINE);

            PendingInteraction search = new PendingInteraction.LibrarySearch(
                    LibrarySearchParams.builder(playerId, List.of()).canFailToFind(true).build(),
                    "p", true);
            assertThat(search.legalOptions()).isEqualTo(new InteractionOptions.LibraryIndexPick(0, true));
        }

        @Test
        @DisplayName("ordering and combat kinds are unenumerated")
        void combinatorialKindsAreUnenumerated() {
            assertThat(new PendingInteraction.Scry(playerId, List.of()).legalOptions())
                    .isEqualTo(InteractionOptions.UNENUMERATED);
            assertThat(new PendingInteraction.AttackerDeclaration(playerId).legalOptions())
                    .isEqualTo(InteractionOptions.UNENUMERATED);
            assertThat(new PendingInteraction.BlockerDeclaration(playerId).legalOptions())
                    .isEqualTo(InteractionOptions.UNENUMERATED);
        }
    }
}
