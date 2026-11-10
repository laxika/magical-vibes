package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.CounterAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.CounterWatch;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassLevelUpEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

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
        // The level's "when this Class becomes level N" abilities, each one object of all its effects (CR 603.3).
        List<List<CardEffect>> levelAbilities = levelUp.gainedEffects().isEmpty()
                ? CounterAbilityReader.printedEffectsByAbility(source.getCard(), levelUp.level() == 2
                        ? CounterWatch.ON_SELF_REACHES_LEVEL_TWO : CounterWatch.ON_SELF_REACHES_LEVEL_THREE)
                : List.of(levelUp.gainedEffects());
        for (List<CardEffect> levelEffects : levelAbilities) {
            queueLevelAbility(gameData, entry, source, levelUp, levelEffects);
        }
    }

    private void queueLevelAbility(GameData gameData, StackEntry entry, Permanent source, ClassLevelUpEffect levelUp,
                                   List<CardEffect> levelEffects) {
        List<CardEffect> gainedEffects = new ArrayList<>(levelEffects);
        if (gainedEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    source.getCard(), entry.getControllerId(), gainedEffects));
        } else if (gainedEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || e.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    source.getCard(), entry.getControllerId(), gainedEffects,
                    "becomes level " + levelUp.level(), source.getId()));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    entry.getControllerId(),
                    source.getCard().getName() + "'s level ability",
                    gainedEffects,
                    null,
                    source.getId()));
        }
    }
}
