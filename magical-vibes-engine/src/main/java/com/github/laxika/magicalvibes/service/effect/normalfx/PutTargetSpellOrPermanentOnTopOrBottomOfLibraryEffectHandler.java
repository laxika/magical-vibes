package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect;
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

/** Resolves Aether Gust's owner choice and library placement. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final StateTriggerService stateTriggerService;
    private final PutTargetSpellOrPermanentIntoLibraryNFromTopEffectHandler topPlacement;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        if (e.destination() == PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect.Destination.CHOOSE) {
            UUID ownerId = findTargetOwner(gameData, targetId);
            if (ownerId == null) {
                return;
            }
            playerInputService.beginChooseModeChoice(gameData, ownerId, entry.getCard(), new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption(
                            "Put it on top",
                            new PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect(
                                    PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect.Destination.TOP)),
                    new ChooseOneEffect.ChooseOneOption(
                            "Put it on the bottom",
                            new PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect(
                                    PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect.Destination.BOTTOM))
            )));
            return;
        }

        if (e.destination() == PutTargetSpellOrPermanentOnTopOrBottomOfLibraryEffect.Destination.TOP) {
            topPlacement.resolve(gameData, entry, new PutTargetSpellOrPermanentIntoLibraryNFromTopEffect(0));
        } else {
            putOnBottom(gameData, entry, targetId);
        }
    }

    private UUID findTargetOwner(GameData gameData, UUID targetId) {
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

    private void putOnBottom(GameData gameData, StackEntry entry, UUID targetId) {
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
