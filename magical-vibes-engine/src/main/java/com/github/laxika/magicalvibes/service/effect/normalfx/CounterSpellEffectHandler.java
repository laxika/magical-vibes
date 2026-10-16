package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
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
        if (targetCardId == null && entry.targetsForEffect(effect).size() == 1) {
            targetCardId = entry.targetsForEffect(effect).getFirst();
        }
        if (targetCardId == null) return;

        UUID counterTargetId = targetCardId;
        StackEntry targetOnStack = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(counterTargetId))
                .findFirst()
                .orElse(null);
        if (targetOnStack != null) {
            entry.setCounteredSpellControllerId(targetOnStack.getControllerId());
            entry.rememberLastKnownPermanentCard(counterTargetId, targetOnStack.getCard());
            Card targetingCard = targetOnStack.getTargetingCard();
            int xSymbols = targetingCard.getManaCost() == null
                    ? 0 : new ManaCost(targetingCard.getManaCost()).getXSymbolCount();
            entry.setEventValue(targetingCard.getManaValue() + targetOnStack.getXValue() * xSymbols);
        }

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;
        rememberTargetPlayerOfCounteredAbility(gameData, targetEntry);

        switch (((CounterSpellEffect) effect).destination()) {
            case GRAVEYARD -> counterSupport.counterSpell(gameData, entry, targetEntry);
            case EXILE -> counterSupport.counterSpellAndExile(gameData, entry, targetEntry);
            case LIBRARY_TOP -> counterSupport.counterSpellAndPutOnTopOfLibrary(gameData, entry, targetEntry);
            case HAND -> counterSupport.counterSpellAndPutInHand(gameData, entry, targetEntry);
            case LIBRARY_BOTTOM -> counterSupport.counterSpellAndPutOnBottomOfLibrary(gameData, entry, targetEntry);
            case LIBRARY_TOP_OR_BOTTOM -> counterOntoChosenLibraryEnd(gameData, entry, targetEntry);
        }
    }

    /**
     * A linked "that player" ability refers to the player chosen when the first ability was put on the
     * stack, so countering that ability (Stifle) must not lose the choice (Laquatus's Champion).
     */
    private void rememberTargetPlayerOfCounteredAbility(GameData gameData, StackEntry countered) {
        UUID playerId = countered.getTargetId();
        UUID sourcePermanentId = countered.getSourcePermanentId();
        if (sourcePermanentId == null || playerId == null || !gameData.playerIds.contains(playerId)
                || countered.getEffectsToResolve().stream().noneMatch(RememberTargetPlayerEffect.class::isInstance)) {
            return;
        }
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.getId().equals(sourcePermanentId)) {
                permanent.setRememberedTargetPlayerId(playerId);
            }
        });
    }

    /**
     * Hinder: the card is countered onto the top of its owner's library, then its controller picks
     * whether it stays there or goes to the bottom.
     */
    private void counterOntoChosenLibraryEnd(GameData gameData, StackEntry entry, StackEntry targetEntry) {
        UUID ownerId = targetEntry.getOwnerId();
        Card countered = counterSupport.counterSpellOntoLibraryPendingEndChoice(gameData, entry, targetEntry);
        if (countered == null) {
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.CounteredSpellLibraryDestinationChoice(
                entry.getControllerId(), ownerId, countered.getId(), countered.getName()));
    }
}
