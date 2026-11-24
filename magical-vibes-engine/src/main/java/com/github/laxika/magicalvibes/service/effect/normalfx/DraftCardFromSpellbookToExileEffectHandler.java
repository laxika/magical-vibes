package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftCardFromSpellbookToExileEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftCardFromSpellbookToExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginDraft(gameData, entry.getControllerId(), entry.getSourcePermanentId(), entry.getCard().getName(),
                (DraftCardFromSpellbookToExileEffect) effect, PendingInteraction.SpellbookDraftToExileChoice.AsEnters.NONE);
    }

    /**
     * Offers the controller up to three distinct spellbook cards, which the controller owns; the chosen one is exiled
     * with the source. Shared by the stack resolution and the as-enters draft of a land or another permanent
     * ({@code EtbTriggerService}, {@code asEnters} saying which entry the answer continues).
     *
     * @return whether a choice was begun (a spellbook with no implemented card offers none)
     */
    public boolean beginDraft(GameData gameData, UUID controllerId, UUID sourcePermanentId, String sourceName,
                              DraftCardFromSpellbookToExileEffect draft,
                              PendingInteraction.SpellbookDraftToExileChoice.AsEnters asEnters) {
        List<Card> spellbook = draft.spellbook().stream()
                .map(reference -> cardCatalog.createCardIfImplemented(
                        reference.setCode(), reference.collectorNumber()))
                .filter(card -> card != null)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (spellbook.isEmpty()) {
            return false;
        }

        Collections.shuffle(spellbook);
        List<Card> offeredCards = new ArrayList<>(spellbook.subList(0, Math.min(3, spellbook.size())));
        offeredCards.forEach(card -> card.setOwnerId(controllerId));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookDraftToExileChoice(
                controllerId, sourcePermanentId, offeredCards, sourceName, draft.faceDown(), asEnters));
        return true;
    }
}
