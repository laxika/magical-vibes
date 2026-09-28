package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachOpposingCreatureThenFightEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves token creation followed by one distinct fight for each opposing creature. */
@Component
@RequiredArgsConstructor
public class CreateTokensForEachOpposingCreatureThenFightEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final FightSupport fightSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensForEachOpposingCreatureThenFightEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var createAndFight = (CreateTokensForEachOpposingCreatureThenFightEffect) effect;
        List<Permanent> opposingCreatures = opposingCreatures(gameData, entry.getControllerId());
        if (opposingCreatures.isEmpty()) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int power = amountEvaluationService.evaluate(
                gameData, createAndFight.tokenTemplate().power(), context);
        int toughness = amountEvaluationService.evaluate(
                gameData, createAndFight.tokenTemplate().toughness(), context);

        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), createAndFight.tokenTemplate(),
                opposingCreatures.size(), entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);

        int pairCount = Math.min(opposingCreatures.size(), createdIds.size());
        for (int i = 0; i < pairCount; i++) {
            Permanent token = gameQueryService.findPermanentById(gameData, createdIds.get(i));
            fightSupport.fight(gameData, entry, token, opposingCreatures.get(i));
        }
    }

    private List<Permanent> opposingCreatures(GameData gameData, UUID controllerId) {
        List<Permanent> creatures = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatures.add(permanent);
                }
            }
        }
        return creatures;
    }
}
