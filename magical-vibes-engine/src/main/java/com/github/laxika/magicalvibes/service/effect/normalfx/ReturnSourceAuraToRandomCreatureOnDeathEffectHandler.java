package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToRandomCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceAuraToRandomCreatureOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAuraToRandomCreatureOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID auraCardId = entry.getCard().getId();
        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, auraCardId);
        UUID auraOwnerId = gameQueryService.findGraveyardOwnerById(gameData, auraCardId);
        if (auraCard == null || auraOwnerId == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in a graveyard)."));
            return;
        }

        List<UUID> validTargetIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (isLegalCreature(gameData, auraCard, auraOwnerId, permanent)) {
                    validTargetIds.add(permanent.getId());
                }
            }
        }

        if (validTargetIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(auraCard, "'s ability fizzles (no creature to attach it to)."));
            return;
        }

        UUID targetId = validTargetIds.get(ThreadLocalRandom.current().nextInt(validTargetIds.size()));
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCardId);
        Permanent auraPermanent = new Permanent(auraCard);
        auraPermanent.setAttachedTo(targetId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraOwnerId, auraPermanent);

        gameLogService.append(gameData, GameLog.builder()
                .card(auraCard)
                .text(" returns to the battlefield attached to ")
                .card(target.getCard())
                .text(" under " + gameData.playerIdToName.get(auraOwnerId) + "'s control.")
                .build());
        log.info("Game {} - {} returns attached to {} (chosen at random)",
                gameData.id, auraCard.getName(), target.getCard().getName());
    }

    private boolean isLegalCreature(GameData gameData, Card auraCard, UUID auraOwnerId, Permanent permanent) {
        if (!gameQueryService.isCreature(gameData, permanent)
                || !auraAttachmentService.canEnchant(gameData, auraCard, auraOwnerId, permanent)) {
            return false;
        }

        if (auraCard.getColors().stream()
                .anyMatch(color -> gameQueryService.hasProtectionFrom(gameData, permanent, color))) {
            return false;
        }
        return !gameQueryService.hasProtectionFromSourceCardTypes(gameData, permanent, auraCard);
    }
}
