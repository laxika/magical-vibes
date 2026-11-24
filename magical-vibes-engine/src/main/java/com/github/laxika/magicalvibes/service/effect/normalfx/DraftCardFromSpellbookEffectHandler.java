package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftCardFromSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftCardFromSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var draft = (DraftCardFromSpellbookEffect) effect;
        entry.recordStep(StepKind.CONJURE, StepOutcome.NONE);
        UUID draftPlayerId = subjectResolutionService.player(gameData, entry, effect, draft.player());
        if (draftPlayerId == null) {
            return;
        }
        offer(gameData, draft, draftPlayerId, entry.getControllerId(), entry.getCard().getName(),
                draft.drafts() - 1);
    }

    /**
     * Offers {@code draft}'s cards to {@code draftPlayerId}, the draft's prompt ending its step; {@code draftsLeft} more
     * drafts follow from {@code SpellbookDraftChoiceInteractionHandler} as each is answered. A spellbook with no
     * implemented card offers nothing.
     *
     * @return whether a prompt began
     */
    public boolean offer(GameData gameData, DraftCardFromSpellbookEffect draft, UUID draftPlayerId, UUID controllerId,
                      String sourceCardName, int draftsLeft) {
        List<Card> spellbook = draft.spellbook().stream()
                .map(reference -> cardCatalog.createCardIfImplemented(
                        reference.setCode(), reference.collectorNumber()))
                .filter(card -> card != null)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (spellbook.isEmpty()) {
            return false;
        }

        Collections.shuffle(spellbook);
        List<Card> offeredCards = draft.conjure()
                ? spellbook
                : new ArrayList<>(spellbook.subList(0, Math.min(3, spellbook.size())));

        if (draft.makeSelectedArtifactCreature() || draft.battlefieldSelectionFollowUp() != null) {
            beginSelectionWithFollowUp(gameData, draft, draftPlayerId, offeredCards);
            return true;
        }

        offeredCards.forEach(card -> card.setOwnerId(draftPlayerId));

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId)
                        + (draft.conjure() ? " conjures" : " drafts") + " a card from "
                        + sourceCardName + "'s spellbook."));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookDraftChoice(
                draftPlayerId, offeredCards, sourceCardName, draft.revealChosenCard(),
                draft.exileChosenCard(), draft.putChosenCardOntoBattlefield(), draft.chosenCardEffects(),
                draft.conjure(), draft, draftsLeft));
        return true;
    }

    private void beginSelectionWithFollowUp(GameData gameData, DraftCardFromSpellbookEffect draft,
                                            UUID playerId, List<Card> offeredCards) {
        offeredCards.forEach(card -> card.setOwnerId(playerId));
        List<UUID> offeredCardIds = offeredCards.stream().map(Card::getId).toList();
        boolean selectedToBattlefield = draft.putChosenCardOntoBattlefield();
        PendingInteraction.LibraryRevealChoice choice = new PendingInteraction.LibraryRevealChoice(
                playerId, offeredCards, offeredCardIds,
                false, !selectedToBattlefield, false, false, true,
                0, null, 1, "Choose a card from this spellbook.",
                false, 1, false, null,
                false, false, false, draft.battlefieldSelectionFollowUp(),
                false, false, false, false, null,
                false, null).withRevealSelected(selectedToBattlefield || draft.revealChosenCard());
        if (draft.makeSelectedArtifactCreature()) {
            choice = choice.withSelectedCardFollowUp(
                    new CardTruePredicate(),
                    new PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect(offeredCardIds));
        }
        interactionHandlerRegistry.begin(gameData, choice);
    }
}
