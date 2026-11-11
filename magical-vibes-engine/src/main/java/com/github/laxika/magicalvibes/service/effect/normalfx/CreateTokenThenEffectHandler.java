package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves "create a token. When you do, ..." (CR 603.12): the tokens are created, then the follow-up is put on the
 * stack as one reflexive ability, or, for a follow-up the card writes as about "that token", one for each token
 * created, with that token as the permanent it acts for.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenThenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenThenEffect createThen = (CreateTokenThenEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();

        createTokenEffectHandler.resolve(gameData, entry, createThen.tokenEffect());
        if (gameData.resolvingMayEffectFromStack || !gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (entry.getCreatedPermanentIds().size() == createdBefore) {
            return;
        }

        if (!createThen.aboutThatToken()) {
            queueReflexiveAbilityEffectHandler.resolve(gameData, entry,
                    new QueueReflexiveAbilityEffect(createThen.thenEffect()));
            return;
        }
        // A replacement effect that makes more tokens makes "that token" each created token in turn. Each ability
        // is put on the stack as a step of this resolution, so one's target is chosen before the next one's is asked.
        List<CardEffect> abilities = new ArrayList<>();
        for (UUID createdId : List.copyOf(entry.getCreatedPermanentIds()
                .subList(createdBefore, entry.getCreatedPermanentIds().size()))) {
            abilities.add(new QueueReflexiveAbilityEffect(createThen.thenEffect(), createdId));
        }
        if (entry.getResolvingEffectIndex() >= 0) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, abilities);
            return;
        }
        for (CardEffect ability : abilities) {
            queueReflexiveAbilityEffectHandler.resolve(gameData, entry, ability);
        }
    }
}
