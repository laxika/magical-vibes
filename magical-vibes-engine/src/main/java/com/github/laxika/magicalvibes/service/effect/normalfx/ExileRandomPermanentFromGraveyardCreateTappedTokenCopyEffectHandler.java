package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sin, Spira's Punishment: exiles a permanent card from the controller's graveyard at random, then creates a tapped
 * token copy of it as one ordered token creation event (CR 616.1). The creation may pause for the replacement order
 * or Jinnie Fay's choice, so when the exiled card is a land card the repeat is spliced in as the entry's next step,
 * where it waits for this iteration's copy and runs as its own exile and creation.
 */
@Component
@RequiredArgsConstructor
public class ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffectHandler
        implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect TAPPED_COPY =
            CreateTokenCopyOfTargetPermanentEffect.tappedTokenCopy();

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final TokenCopySupport tokenCopySupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var controllerId = entry.getControllerId();

        List<Card> permanentCards = gameData.playerGraveyards
                .getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.getType().isPermanentType())
                .toList();
        if (permanentCards.isEmpty()) {
            return;
        }

        Card exiledCard = permanentCards.get(ThreadLocalRandom.current().nextInt(permanentCards.size()));
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiledCard.getId());
        exileService.exileCard(gameData, controllerId, exiledCard);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " exiles ", exiledCard,
                " at random from their graveyard."));

        if (exiledCard.hasType(CardType.LAND)) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(effect));
        }
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(exiledCard), null, TAPPED_COPY);
    }
}
