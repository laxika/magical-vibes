package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceAuraToCreatureOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAuraToCreatureOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID auraCardId = entry.getCard().getId();
        UUID auraControllerId = entry.getControllerId();
        List<Card> controllerGraveyard = gameData.playerGraveyards.get(auraControllerId);
        Card auraCard = controllerGraveyard == null ? null : controllerGraveyard.stream()
                .filter(card -> card.getId().equals(auraCardId))
                .findFirst()
                .orElse(null);

        if (auraCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in your graveyard)."));
            log.info("Game {} - {} not found in controller's graveyard, death trigger fizzles",
                    gameData.id, entry.getCard().getName());
            return;
        }

        List<UUID> validTargetIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && auraAttachmentService.canEnchant(gameData, auraCard, auraControllerId, permanent)) {
                    validTargetIds.add(permanent.getId());
                }
            }
        }

        if (validTargetIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(auraCard,
                    "'s ability fizzles (no creature to attach it to)."));
            log.info("Game {} - {} death trigger fizzles (no legal creature)",
                    gameData.id, auraCard.getName());
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCardId);

        if (validTargetIds.size() == 1) {
            attachAura(gameData, auraCard, auraControllerId, validTargetIds.getFirst());
            return;
        }

        gameData.interaction.setPendingAuraCard(auraCard);
        gameData.interaction.setPendingAuraOwnerId(auraControllerId);
        playerInputService.beginPermanentChoice(gameData, auraControllerId, validTargetIds,
                "Choose a creature to attach " + auraCard.getName() + " to.");
    }

    private void attachAura(GameData gameData, Card auraCard, UUID auraControllerId, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        Permanent auraPermanent = new Permanent(auraCard);
        auraPermanent.setAttachedTo(targetId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraControllerId, auraPermanent);

        gameLogService.append(gameData, GameLog.builder().card(auraCard)
                .text(" returns to the battlefield attached to ").card(target.getCard())
                .text(" under " + gameData.playerIdToName.get(auraControllerId) + "'s control.").build());
        log.info("Game {} - {} returns attached to {}", gameData.id,
                auraCard.getName(), target.getCard().getName());
    }
}
