package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardRecipient;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftCardFromSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftCardFromSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var draft = (DraftCardFromSpellbookEffect) effect;
        List<Card> spellbook = draft.cardNames().stream()
                .map(this::findCard)
                .filter(card -> card != null)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (spellbook.isEmpty()) {
            return;
        }

        Collections.shuffle(spellbook);
        List<Card> offeredCards = new ArrayList<>(spellbook.subList(0, Math.min(3, spellbook.size())));
        UUID draftPlayerId = draft.recipient() == DraftCardRecipient.TARGET_PLAYER
                ? entry.getTargetId() : entry.getControllerId();

        if (draft.makeSelectedArtifactCreature() || draft.battlefieldSelectionFollowUp() != null) {
            beginSelectionWithFollowUp(gameData, draft, draftPlayerId, offeredCards);
            return;
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId()) + " drafts a card from "
                        + entry.getCard().getName() + "'s spellbook."));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookDraftChoice(
                draftPlayerId, offeredCards, entry.getCard().getName(), draft.revealChosenCard(),
                draft.exileChosenCard(), draft.putChosenCardOntoBattlefield(), draft.chosenCardEffects()));
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
                false, null);
        if (draft.makeSelectedArtifactCreature()) {
            choice = choice.withSelectedCardFollowUp(
                    new CardTruePredicate(),
                    new PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect(offeredCardIds));
        }
        interactionHandlerRegistry.begin(gameData, choice);
    }

    private Card findCard(String cardName) {
        Set<String> inspectedClasses = new HashSet<>();
        for (CardSet cardSet : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(cardSet)) {
                if (!inspectedClasses.add(printing.cardClassName())) {
                    continue;
                }
                Card card = printing.createCard();
                if (cardName.equals(card.getName())) {
                    return card;
                }
            }
        }
        return null;
    }
}
