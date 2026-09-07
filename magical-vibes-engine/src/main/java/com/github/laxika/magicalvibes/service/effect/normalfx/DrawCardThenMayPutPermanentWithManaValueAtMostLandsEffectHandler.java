package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardThenMayPutPermanentWithManaValueAtMostLandsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DrawCardThenMayPutPermanentWithManaValueAtMostLandsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardThenMayPutPermanentWithManaValueAtMostLandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int landCount = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of())
                .stream()
                .filter(permanent -> gameQueryService.isLand(gameData, permanent))
                .mapToInt(ignored -> 1)
                .sum();

        PutCardToBattlefieldEffect putPermanent = new PutCardToBattlefieldEffect(
                new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardMaxManaValuePredicate(landCount))),
                "permanent", true);
        List<CardEffect> followUps = List.of(
                new DrawCardEffect(1),
                new MayEffect(putPermanent,
                        "You may put a permanent card with mana value " + landCount
                                + " or less from your hand onto the battlefield tapped."));

        int effectIndex = findEffectIndex(entry, effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Land-count leave effect is not on its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, followUps);
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                return i;
            }
        }
        return -1;
    }
}
