package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassLevelUpEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GameLogService gameLogService;

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
        if (levelUp.gainedEffects().isEmpty()) {
            gameData.rulesEvents.occur(CounterChangeEvent.of(CounterChangeEvent.Kind.LEVEL_REACHED, source,
                    entry.getControllerId(), null, levelUp.level()));
            return;
        }
        Card card = source.getCard();
        TriggeredAbility ability = new TriggeredAbility(levelUp.level() == 2
                ? Triggers.reachesLevelTwo() : Triggers.reachesLevelThree(),
                levelUp.gainedEffects().toArray(CardEffect[]::new));
        if (triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, source,
                entry.getControllerId(), card.getName() + "'s triggered ability",
                ConditionContext.forPermanent(source, entry.getControllerId()), stamped -> {
                }))) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
        }
    }

}
