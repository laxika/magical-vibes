package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfExiledCardAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfExiledCardAttackingOpponentsEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfExiledCardAttackingOpponentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> exiledCards = gameData.getCardsExiledByPermanent(entry.getSourcePermanentId());
        if (exiledCards.isEmpty()) {
            return;
        }

        Card exiledCard = exiledCards.get(0);
        List<UUID> opponentIds = opponentIds(gameData, entry.getControllerId());
        if (opponentIds.isEmpty()) {
            return;
        }

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        Set<CardType> enterTappedTypesSnapshot = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        CreateTokenCopyOfTargetPermanentEffect copyEffect = new CreateTokenCopyOfTargetPermanentEffect();

        for (UUID opponentId : opponentIds) {
            for (int copy = 0; copy < tokenMultiplier; copy++) {
                Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(exiledCard, copyEffect);
                tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                        gameData, entry.getControllerId(), tokenCard);
                Permanent tokenPermanent = new Permanent(tokenCard);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, entry.getControllerId(), tokenPermanent,
                        enterTappedTypesSnapshot, simultaneouslyEntered);
                simultaneouslyEntered.add(tokenPermanent);

                tokenPermanent.tap();
                if (tokenCard.hasType(CardType.CREATURE)) {
                    tokenPermanent.setAttacking(true);
                    tokenPermanent.setAttackTarget(opponentId);
                }
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));

                gameLogService.append(gameData, GameLog.textCardText("A token copy of ", exiledCard, " is created."));
                log.info("Game {} - Token copy of {} created attacking {} via {}", gameData.id,
                        exiledCard.getName(), opponentId, entry.getCard().getName());
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, entry.getControllerId(), tokenCard, null, false);
            }
        }
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
