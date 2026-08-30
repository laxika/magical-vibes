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

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (ownerId == null || dyingCard == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, dyingCard);

        var copyOptions = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.SPIRIT), Set.of(), null, null, Map.of());
        int tokenCount = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId(), true);
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> batch = new ArrayList<>();
        for (int i = 0; i < tokenCount; i++) {
            Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(dyingCard, copyOptions);
            tokenCard.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                    new ReturnLinkedCardToOwnerGraveyardEffect());

            Permanent tokenPermanent = new Permanent(tokenCard);
            tokenPermanent.setChosenPermanentId(dyingCardId);
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
                gameData.id, entry.getCard().getName(), dyingCard.getName());
    }
}
