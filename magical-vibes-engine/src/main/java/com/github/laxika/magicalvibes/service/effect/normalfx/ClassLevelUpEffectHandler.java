package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.CounterWatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassLevelUpEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ClassLevelUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ClassLevelUpEffect levelUp = (ClassLevelUpEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.getClassLevel() != levelUp.level() - 1) {
            return;
        }

        source.setClassLevel(levelUp.level());
        // The level's "when this Class becomes level N" abilities, each whole (CR 603.3); a level whose card writes the
        // ability's effects into its level-up is that ability.
        CounterWatch watch = levelUp.level() == 2
                ? CounterWatch.ON_SELF_REACHES_LEVEL_TWO : CounterWatch.ON_SELF_REACHES_LEVEL_THREE;
        if (levelUp.gainedEffects().isEmpty()) {
            permanentCounterSupport.putOwnCounterAbilities(gameData, source, entry.getControllerId(), watch);
        } else {
            permanentCounterSupport.putOwnCounterAbility(gameData, new TriggeredAbility(watch.trigger(),
                    levelUp.gainedEffects().toArray(CardEffect[]::new)), source, entry.getControllerId());
        }
    }

}
