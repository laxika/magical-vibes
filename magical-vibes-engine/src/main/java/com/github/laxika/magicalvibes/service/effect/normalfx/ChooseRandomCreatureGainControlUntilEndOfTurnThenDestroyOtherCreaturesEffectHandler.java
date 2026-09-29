package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomCreatureGainControlUntilEndOfTurnThenDestroyOtherCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves The Nipton Lottery's random creature selection and follow-up effects. */
@Component
@RequiredArgsConstructor
public class ChooseRandomCreatureGainControlUntilEndOfTurnThenDestroyOtherCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final TapUntapSupport tapUntapSupport;
    private final GrantKeywordEffectHandler grantKeywordEffectHandler;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseRandomCreatureGainControlUntilEndOfTurnThenDestroyOtherCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> creatures = new ArrayList<>();
        gameData.forEachBattlefield((ignored, battlefield) -> battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .forEach(creatures::add));
        if (creatures.isEmpty()) {
            return;
        }

        Permanent chosen = creatures.get(ThreadLocalRandom.current().nextInt(creatures.size()));
        creatureControlService.applyControlEffect(gameData, entry.getControllerId(), chosen,
                new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                ControlDuration.END_OF_TURN.toEffectDuration(), null, entry.getCard().getName());
        tapUntapSupport.untapPermanent(gameData, chosen);
        grantKeywordEffectHandler.grantToPermanent(gameData, entry, chosen, Set.of(Keyword.HASTE));

        destructionSupport.performDestroyAllCreaturesExcept(
                gameData, entry.getCard().getName(), List.of(chosen.getId()));
    }
}
