package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceAuraToOpponentCreatureOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAuraToOpponentCreatureOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnSourceAuraToOpponentCreatureOnDeathEffect) effect;

        UUID auraCardId = entry.getCard().getId();
        UUID auraOwnerId = entry.getControllerId();
        UUID enchantedCreatureControllerId = e.enchantedCreatureControllerId();

        if (enchantedCreatureControllerId == null) {
            log.info("Game {} - {} death trigger fizzles (no enchanted creature controller)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // Find the aura card in the graveyard
        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, auraCardId);
        if (auraCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} not found in graveyard, death trigger fizzles",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // Find all creatures controlled by opponents of the dying creature's controller
        List<UUID> validTargetIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(enchantedCreatureControllerId)) continue;

            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (gameQueryService.isCreature(gameData, p)) {
                    validTargetIds.add(p.getId());
                }
            }
        }

        if (validTargetIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (no opponent creatures to attach to)."));
            log.info("Game {} - {} death trigger fizzles (no opponent creatures)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // Remove aura from graveyard
        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCardId);

        if (validTargetIds.size() == 1) {
            // Auto-attach when only one valid target
            Permanent target = gameQueryService.findPermanentById(gameData, validTargetIds.getFirst());
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(target.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraOwnerId, auraPerm);

            String ownerName = gameData.playerIdToName.get(auraOwnerId);
            String logEntry = auraCard.getName() + " returns to the battlefield attached to "
                    + target.getCard().getName() + " under " + ownerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(auraCard).text(" returns to the battlefield attached to ").card(target.getCard()).text(" under " + ownerName + "'s control.").build());
            log.info("Game {} - {} returns attached to {} (auto-selected)",
                    gameData.id, auraCard.getName(), target.getCard().getName());
        } else {
            // Multiple valid targets — let the dying creature's controller choose
            gameData.interaction.setPendingAuraCard(auraCard);
            gameData.interaction.setPendingAuraOwnerId(auraOwnerId);

            playerInputService.beginPermanentChoice(gameData, enchantedCreatureControllerId, validTargetIds,
                    "Choose a creature to attach " + auraCard.getName() + " to.");
        }
    }
}
