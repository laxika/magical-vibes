package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Resolves a continuation by placing its effect on the stack as a reflexive ability. */
@Component
@RequiredArgsConstructor
public class QueueReflexiveAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardTargetingSupport graveyardTargetingSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return QueueReflexiveAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        QueueReflexiveAbilityEffect queueEffect = (QueueReflexiveAbilityEffect) effect;
        if (graveyardTargetingSupport.findTarget(List.of(queueEffect.effect())) != null
                || queueEffect.effect().targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    entry.getCard(), entry.getControllerId(), List.of(queueEffect.effect())));
            return;
        }
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s reflexive ability",
                new ArrayList<>(List.of(queueEffect.effect())),
                0,
                entry.getSourcePermanentId()));
    }
}
