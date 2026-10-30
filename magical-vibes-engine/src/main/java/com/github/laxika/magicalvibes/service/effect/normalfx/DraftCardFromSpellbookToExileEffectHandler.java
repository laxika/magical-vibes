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
                (DraftCardFromSpellbookToExileEffect) effect);
    }

    /**
     * Offers the controller up to three distinct spellbook cards; the chosen one is exiled with the source.
     * Shared by the stack resolution and the as-enters draft of a land ({@code EtbTriggerService}).
     */
    public void beginDraft(GameData gameData, UUID controllerId, UUID sourcePermanentId, String sourceName,
                           DraftCardFromSpellbookToExileEffect draft) {
        List<Card> spellbook = draft.spellbook().stream()
                .map(reference -> cardCatalog.createCardIfImplemented(
                        reference.setCode(), reference.collectorNumber()))
                .filter(card -> card != null)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (spellbook.isEmpty()) {
            return;
        }

        Collections.shuffle(spellbook);
        List<Card> offeredCards = new ArrayList<>(spellbook.subList(0, Math.min(3, spellbook.size())));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SpellbookDraftToExileChoice(
                controllerId, sourcePermanentId, offeredCards, sourceName, draft.faceDown()));
    }
}
