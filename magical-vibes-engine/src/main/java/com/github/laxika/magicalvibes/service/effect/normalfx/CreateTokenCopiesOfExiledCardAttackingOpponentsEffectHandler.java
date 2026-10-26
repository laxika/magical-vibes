package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfExiledCardAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Creates, for each opponent, a tapped copy of each card the source exiled attacking that opponent, as one token
 * creation event the controller orders the replacements of (CR 616.1). Every token the event creates, added tokens
 * such as Queen Allenal's Soldier included, is sacrificed at the beginning of the controller's next end step.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfExiledCardAttackingOpponentsEffectHandler implements NormalEffectHandlerBean {

    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfExiledCardAttackingOpponentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> exiledCards = gameData.getCardsExiledByPermanent(entry.getSourcePermanentId());
        List<Card> cardsToCopy = exiledCards.isEmpty() && entry.getSourceZone() == Zone.GRAVEYARD
                ? List.of(entry.getCard())
                : exiledCards;
        if (cardsToCopy.isEmpty()) {
            return;
        }
        UUID controllerId = entry.getControllerId();
        List<UUID> opponentIds = opponentIds(gameData, controllerId);
        if (opponentIds.isEmpty()) {
            return;
        }

        var copyEffect = (CreateTokenCopiesOfExiledCardAttackingOpponentsEffect) effect;
        tokenCopySupport.createTokenCopiesAttackingEachOpponent(gameData, entry, List.copyOf(cardsToCopy),
                controllerId, new CreateTokenCopyOfTargetPermanentEffect(copyEffect.grantHaste(), false, false, true),
                opponentIds, token -> gameData.queueDelayedAction(new DelayedPermanentAction(
                        token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP,
                        false, null, controllerId, controllerId, false, entry.getCard())));
    }

    private List<UUID> opponentIds(GameData gameData, UUID controllerId) {
        List<UUID> opponentIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (gameData.playerIds.contains(playerId) && !playerId.equals(controllerId)) {
                opponentIds.add(playerId);
            }
        }
        return opponentIds;
    }
}
