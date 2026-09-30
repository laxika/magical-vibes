package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesWithDifferentPowersBoostAndGrantVigilanceEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.laxika.magicalvibes.model.Keyword.VIGILANCE;

/** Resolves Sigardian Zealot's non-targeted distinct-power creature choice. */
@Component
@RequiredArgsConstructor
public class ChooseCreaturesWithDifferentPowersBoostAndGrantVigilanceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreaturesWithDifferentPowersBoostAndGrantVigilanceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> creatureIds = new ArrayList<>();
        gameData.forEachPermanent((ignoredControllerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureIds.add(permanent.getId());
            }
        });

        if (creatureIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                creatureIds,
                creatureIds.size(),
                new MultiPermanentChoiceContext.ChooseCreaturesWithDifferentPowersBoostAndGrantVigilance(),
                "Choose any number of creatures with different powers.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int sourcePower = amountEvaluationService.evaluate(
                gameData, new SourcePower(),
                AmountContext.forStackEntry(entry, source));

        Set<Integer> powers = new HashSet<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !gameQueryService.isCreature(gameData, permanent)
                    || !powers.add(gameQueryService.getEffectivePower(gameData, permanent))) {
                return;
            }
        }

        BuffTargetCreatureIndefinitelyEffect boost =
                new BuffTargetCreatureIndefinitelyEffect(sourcePower, sourcePower);
        GrantKeywordEffect vigilance = new GrantKeywordEffect(VIGILANCE, GrantScope.TARGET);
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(), boost, permanent.getId(), null, null,
                    EffectDuration.UNTIL_END_OF_TURN, 0));
            if (!gameQueryService.cantHaveOrGainKeyword(gameData, permanent, VIGILANCE)) {
                permanent.getGrantedKeywords().add(VIGILANCE);
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                        entry.getControllerId(), vigilance, permanent.getId(), null, null,
                        EffectDuration.UNTIL_END_OF_TURN, 0));
            }
        }
    }
}
