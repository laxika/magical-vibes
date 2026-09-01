package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the owner choice and library placement for Endless Detour. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final StateTriggerService stateTriggerService;
    private final PutTargetSpellOrPermanentIntoLibraryNFromTopEffectHandler topPlacement;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        if (e.destination() == PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect.Destination.CHOOSE) {
            UUID ownerId = findTargetOwner(gameData, targetId);
            if (ownerId == null) {
                return;
            }
            playerInputService.beginChooseModeChoice(gameData, ownerId, entry.getCard(), new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption(
                            "Put it on top",
                            new PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect(
                                    PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect.Destination.TOP)),
                    new ChooseOneEffect.ChooseOneOption(
                            "Put it on the bottom",
                            new PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect(
                                    PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect.Destination.BOTTOM))
            )));
            return;
        }

        if (isGraveyardTarget(gameData, targetId)) {
            moveGraveyardCard(gameData, targetId, e.destination());
        } else if (e.destination()
                == PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect.Destination.TOP) {
            topPlacement.resolve(gameData, entry, new PutTargetSpellOrPermanentIntoLibraryNFromTopEffect(0));
        } else {
            putSpellOrPermanentOnBottom(gameData, targetId);
        }
    }

    private boolean isGraveyardTarget(GameData gameData, UUID targetId) {
        return gameQueryService.findGraveyardOwnerById(gameData, targetId) != null;
    }

    private UUID findTargetOwner(GameData gameData, UUID targetId) {
        UUID graveyardOwner = gameQueryService.findGraveyardOwnerById(gameData, targetId);
        if (graveyardOwner != null) {
            return graveyardOwner;
        }

        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        if (permanent != null) {
            return gameData.defaultControllerOf(targetId);
        }

        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (targetSpell == null || targetSpell.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || targetSpell.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            return null;
        }
        Card card = targetSpell.getCard();
        return card.getOwnerId() != null ? card.getOwnerId() : targetSpell.getOwnerId();
    }

    private void moveGraveyardCard(
            GameData gameData, UUID targetId,
            PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect.Destination destination) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, targetId);
        if (targetCard == null || ownerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetId);
        GraveyardChoiceDestination graveyardDestination = destination
                == PutTargetSpellOrPermanentOrGraveyardCardOnTopOrBottomOfLibraryEffect.Destination.TOP
                ? GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY
                : GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY;
        graveyardReturnSupport.moveCardToDestination(gameData, ownerId, targetCard, graveyardDestination,
                null, null, false);
    }

    private void putSpellOrPermanentOnBottom(GameData gameData, UUID targetId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        if (permanent != null) {
            if (permanentRemovalService.removePermanentToLibraryBottom(gameData, permanent)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(permanent.getCard(), " is put on the bottom of its owner's library."));
                log.info("Game {} - {} put on bottom of library", gameData.id, permanent.getCard().getName());
            }
            permanentRemovalService.removeOrphanedAuras(gameData);
            return;
        }

        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (targetSpell == null || targetSpell.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || targetSpell.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            return;
        }

        gameData.stack.remove(targetSpell);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, targetSpell);
        if (!targetSpell.isCopy()) {
            Card spell = targetSpell.getCard();
            UUID ownerId = spell.getOwnerId() != null ? spell.getOwnerId() : targetSpell.getOwnerId();
            gameData.playerDecks.get(ownerId).add(spell);
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(targetSpell.getCard())
                .text(" is put on the bottom of its owner's library.")
                .build());
        log.info("Game {} - spell {} put on bottom of library", gameData.id, targetSpell.getCard().getName());
    }
}
