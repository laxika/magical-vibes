package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToSharedTypeCreatureOnDeathEffect;
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

/**
 * Resolves Reins of the Vinesteed's death trigger (already gated by a {@code MayEffect}): return the
 * aura from its owner's graveyard to the battlefield attached to a creature that shares a creature
 * type with the creature that died. The dying creature's ID is baked into the effect; its creature
 * types are read from the graveyard as last-known information (Changeling-aware).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceAuraToSharedTypeCreatureOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceAuraToSharedTypeCreatureOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnSourceAuraToSharedTypeCreatureOnDeathEffect) effect;

        UUID auraCardId = entry.getCard().getId();
        UUID auraOwnerId = entry.getControllerId();
        UUID dyingCreatureCardId = e.dyingCreatureCardId();

        if (dyingCreatureCardId == null) {
            log.info("Game {} - {} death trigger fizzles (no dying creature card ID)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // Find the aura card in the graveyard
        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, auraCardId);
        if (auraCard == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (card not in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} not found in graveyard, death trigger fizzles",
                    gameData.id, entry.getCard().getName());
            return;
        }

        // Read the dying creature's creature types from the graveyard (last-known information).
        Card dyingCreatureCard = gameQueryService.findCardInGraveyardById(gameData, dyingCreatureCardId);
        List<CardSubtype> dyingTypes = new ArrayList<>();
        boolean dyingIsChangeling = false;
        if (dyingCreatureCard != null) {
            dyingTypes.addAll(dyingCreatureCard.getSubtypes());
            dyingIsChangeling = dyingCreatureCard.getKeywords().contains(Keyword.CHANGELING);
        }

        if (dyingTypes.isEmpty() && !dyingIsChangeling) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(auraCard, "'s ability fizzles (dying creature had no creature type)."));
            log.info("Game {} - {} death trigger fizzles (dying creature has no creature type)",
                    gameData.id, auraCard.getName());
            return;
        }

        // Find every creature on the battlefield that shares a creature type with the dying creature.
        final boolean dyingChangeling = dyingIsChangeling;
        List<UUID> validTargetIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (!gameQueryService.isCreature(gameData, p)) continue;

                List<CardSubtype> pTypes = new ArrayList<>(p.getCard().getSubtypes());
                pTypes.addAll(p.getTransientSubtypes());
                boolean pIsChangeling = p.hasKeyword(Keyword.CHANGELING);

                boolean sharesType = (pIsChangeling && (dyingChangeling || !dyingTypes.isEmpty()))
                        || (dyingChangeling && !pTypes.isEmpty())
                        || pTypes.stream().anyMatch(dyingTypes::contains);

                if (sharesType) {
                    validTargetIds.add(p.getId());
                }
            }
        }

        if (validTargetIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(auraCard, "'s ability fizzles (no creature shares a creature type)."));
            log.info("Game {} - {} death trigger fizzles (no shared-type creatures)",
                    gameData.id, auraCard.getName());
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
            // Multiple valid targets — let the aura's owner choose
            gameData.interaction.setPendingAuraCard(auraCard);
            gameData.interaction.setPendingAuraOwnerId(auraOwnerId);

            playerInputService.beginPermanentChoice(gameData, auraOwnerId, validTargetIds,
                    "Choose a creature to attach " + auraCard.getName() + " to.");
        }
    }
}
