package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Puts a target spell or creature on top of its owner's library and pauses for that owner to pick
 * whether it stays on top or moves to the bottom.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final StateTriggerService stateTriggerService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        if (permanent != null) {
            movePermanentToTopAndBeginChoice(gameData, permanent);
            return;
        }

        StackEntry spell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (spell == null || isAbility(spell)) {
            return;
        }

        gameData.stack.remove(spell);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, spell);

        if (spell.isCopy()) {
            return;
        }

        Card card = spell.getCard();
        UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : spell.getOwnerId();
        List<Card> library = ownerId == null ? null : gameData.playerDecks.get(ownerId);
        if (library == null) {
            return;
        }
        library.addFirst(card);
        beginChoice(gameData, ownerId, card);
    }

    private void movePermanentToTopAndBeginChoice(GameData gameData, Permanent permanent) {
        Card card = permanent.getCard();
        if (!permanentRemovalService.removePermanentToLibraryTop(gameData, permanent)) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        UUID ownerId = findLibraryOwner(gameData, card);
        if (ownerId == null) {
            return;
        }
        beginChoice(gameData, ownerId, card);
    }

    private UUID findLibraryOwner(GameData gameData, Card card) {
        return gameData.playerDecks.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(c -> c.getId().equals(card.getId())))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private void beginChoice(GameData gameData, UUID ownerId, Card card) {
        List<Card> library = gameData.playerDecks.get(ownerId);
        if (library == null || library.stream().noneMatch(c -> c.getId().equals(card.getId()))) {
            return;
        }
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.TargetLibraryDestinationChoice(ownerId, card.getId(), card.getName()));
        log.info("Game {} - awaiting {} to choose the library destination for {}", gameData.id,
                gameData.playerIdToName.get(ownerId), card.getName());
    }

    private static boolean isAbility(StackEntry entry) {
        return entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
    }
}
