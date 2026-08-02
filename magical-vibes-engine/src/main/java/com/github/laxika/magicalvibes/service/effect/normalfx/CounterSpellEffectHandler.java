package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CounterSpellEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        switch (((CounterSpellEffect) effect).destination()) {
            case GRAVEYARD -> counterSupport.counterSpell(gameData, entry, targetEntry);
            case EXILE -> counterSupport.counterSpellAndExile(gameData, entry, targetEntry);
            case LIBRARY_TOP -> counterSupport.counterSpellAndPutOnTopOfLibrary(gameData, entry, targetEntry);
            case LIBRARY_TOP_OR_BOTTOM -> counterOntoChosenLibraryEnd(gameData, entry, targetEntry);
        }
    }

    /**
     * Hinder: the card is countered onto the top of its owner's library, then its controller picks
     * whether it stays there or goes to the bottom.
     */
    private void counterOntoChosenLibraryEnd(GameData gameData, StackEntry entry, StackEntry targetEntry) {
        UUID ownerId = targetEntry.getControllerId();
        Card countered = counterSupport.counterSpellOntoLibraryPendingEndChoice(gameData, entry, targetEntry);
        if (countered == null) {
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.CounteredSpellLibraryDestinationChoice(
                entry.getControllerId(), ownerId, countered.getId(), countered.getName()));
    }
}
