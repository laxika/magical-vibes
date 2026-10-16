package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureAndCreateSpiritTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnLinkedCardToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Hofri Ghostforge's death trigger by exiling the dying card and creating linked Spirit
 * copies of it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileDyingCreatureAndCreateSpiritTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDyingCreatureAndCreateSpiritTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var hofriEffect = (ExileDyingCreatureAndCreateSpiritTokenCopyEffect) effect;
        UUID dyingCardId = hofriEffect.dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        // The token copies the creature as it last existed on the battlefield (its copy values, e.g. a
        // Clone's copied creature), not the card it left in the graveyard.
        Permanent departed = gameData.departedPermanentSnapshots.get(dyingCardId);

        // A copy's battlefield card is not the physical card that went to the graveyard.
        UUID graveyardCardId = dyingCardId;
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, graveyardCardId);
        if (dyingCard == null && departed != null && departed.getOriginalCard() != null) {
            graveyardCardId = departed.getOriginalCard().getId();
            dyingCard = gameQueryService.findCardInGraveyardById(gameData, graveyardCardId);
        }
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, graveyardCardId);
        if (ownerId == null || dyingCard == null) {
            return;
        }
        Card copySource = departed != null && departed.getCard() != null ? departed.getCard() : dyingCard;

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, graveyardCardId);
        exileService.exileCard(gameData, ownerId, dyingCard);

        var copyOptions = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.SPIRIT), Set.of(), null, null, Map.of());
        List<CardSubtype> tokenSubtypes = copySource.getSubtypes() == null
                ? new ArrayList<>()
                : new ArrayList<>(copySource.getSubtypes());
        if (!tokenSubtypes.contains(CardSubtype.SPIRIT)) {
            tokenSubtypes.add(CardSubtype.SPIRIT);
        }
        int tokenCount = gameQueryService.getTokenCreationAmount(
                gameData, entry.getControllerId(), 1, tokenSubtypes, true);
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> batch = new ArrayList<>();
        for (int i = 0; i < tokenCount; i++) {
            Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(copySource, copyOptions);
            tokenCard.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                    new ReturnLinkedCardToOwnerGraveyardEffect());

            Permanent tokenPermanent = new Permanent(tokenCard);
            tokenPermanent.setChosenPermanentId(graveyardCardId);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, entry.getControllerId(), tokenPermanent,
                    enterTappedTypes, batch);
            batch.add(tokenPermanent);
            entry.getCreatedPermanentIds().add(tokenPermanent.getId());
            if (tokenCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, entry.getControllerId(), tokenCard, null, false);
            }
        }

        gameLogService.append(gameData, GameLog.textCardText(
                "A Spirit token copy of ", dyingCard, " is created."));
        log.info("Game {} - {} creates a Spirit token copy of {}",
                gameData.id, entry.getCard().getName(), copySource.getName());
    }
}
