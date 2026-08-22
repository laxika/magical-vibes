package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachOtherControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEachOtherControlledPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEachOtherControlledPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfEachOtherControlledPermanentEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        List<Permanent> sourcePermanents = new ArrayList<>();
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (permanent.getId().equals(entry.getSourcePermanentId())) {
                continue;
            }
            if (predicateEvaluationService.matchesPermanentPredicate(
                    permanent, copyEffect.filter(), filterContext)) {
                sourcePermanents.add(permanent);
            }
        }

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        List<Permanent> batch = new ArrayList<>();
        List<UUID> createdTokenIds = new ArrayList<>();
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        for (Permanent sourcePermanent : sourcePermanents) {
            Card sourceCard = sourcePermanent.getCard();
            for (int copy = 0; copy < tokenMultiplier; copy++) {
                Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(
                        sourceCard, new CreateTokenCopyOfTargetPermanentEffect());
                if (sourceCard.getType() == CardType.PLANESWALKER) {
                    tokenCard.setLoyalty(sourceCard.getLoyalty());
                }
                tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                        gameData, entry.getControllerId(), tokenCard);

                Permanent tokenPermanent = new Permanent(tokenCard);
                if (tokenCard.getType() == CardType.PLANESWALKER) {
                    tokenPermanent.setCounterCount(CounterType.LOYALTY,
                            tokenCard.getLoyalty() == null ? 0 : tokenCard.getLoyalty());
                    tokenPermanent.setSummoningSick(false);
                }
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, entry.getControllerId(), tokenPermanent, enterTappedTypes, batch);
                batch.add(tokenPermanent);
                entry.getCreatedPermanentIds().add(tokenPermanent.getId());
                createdTokenIds.add(tokenPermanent.getId());

                gameLogService.append(gameData, GameLog.textCardText("A token copy of ", sourceCard, " is created."));
                log.info("Game {} - Token copy of {} created via {}", gameData.id, sourceCard.getName(),
                        entry.getCard() == null ? "ability" : entry.getCard().getName());
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, entry.getControllerId(), tokenCard, null, false);
            }
        }
        battlefieldEntryService.checkAllyTokenEntersTriggers(
                gameData, entry.getControllerId(), createdTokenIds.size());
    }
}
