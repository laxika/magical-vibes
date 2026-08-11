package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToChosenCreatureOnLeaveEffect;
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
public class ReturnSourceAuraToChosenCreatureOnLeaveEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final AuraAttachmentService auraAttachmentService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAuraToChosenCreatureOnLeaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnSourceAuraToChosenCreatureOnLeaveEffect) effect;
        UUID auraCardId = entry.getCard().getId();
        UUID chooserId = returnEffect.leavingPermanentControllerId();

        if (chooserId == null) {
            log.info("Game {} - {} leave trigger fizzles (no former enchanted creature controller)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, auraCardId);
        if (auraCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} not found in graveyard, leave trigger fizzles",
                    gameData.id, entry.getCard().getName());
            return;
        }

        UUID auraOwnerId = gameQueryService.findGraveyardOwnerById(gameData, auraCardId);
        if (auraOwnerId == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card has no graveyard owner)."));
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
                        && auraAttachmentService.canEnchant(gameData, auraCard, auraOwnerId, permanent)
                        && !gameQueryService.hasProtectionFromSource(gameData, permanent, auraCard)) {
                    validTargetIds.add(permanent.getId());
                }
            }
        }

        if (validTargetIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (no legal creature to enchant)."));
            log.info("Game {} - {} leave trigger fizzles (no legal creature)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCardId);

        if (validTargetIds.size() == 1) {
            Permanent target = gameQueryService.findPermanentById(gameData, validTargetIds.getFirst());
            Permanent auraPermanent = new Permanent(auraCard);
            auraPermanent.setAttachedTo(target.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraOwnerId, auraPermanent);

            String ownerName = gameData.playerIdToName.get(auraOwnerId);
            gameLogService.append(gameData, GameLog.builder()
                    .card(auraCard)
                    .text(" returns to the battlefield attached to ")
                    .card(target.getCard())
                    .text(" under " + ownerName + "'s control.")
                    .build());
            log.info("Game {} - {} returns attached to {} (auto-selected)",
                    gameData.id, auraCard.getName(), target.getCard().getName());
            return;
        }

        gameData.interaction.setPendingAuraCard(auraCard);
        gameData.interaction.setPendingAuraOwnerId(auraOwnerId);
        playerInputService.beginPermanentChoice(gameData, chooserId, validTargetIds,
                "Choose a creature to attach " + auraCard.getName() + " to.");
    }
}
