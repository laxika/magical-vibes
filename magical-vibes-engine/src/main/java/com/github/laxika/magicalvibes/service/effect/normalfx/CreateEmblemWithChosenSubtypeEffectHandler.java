package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemWithChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves an emblem whose continuous effect is restricted to a creature type chosen on resolution. */
@Component
@RequiredArgsConstructor
public class CreateEmblemWithChosenSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final CreateEmblemEffectHandler createEmblemEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateEmblemWithChosenSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateEmblemWithChosenSubtypeEffect emblemEffect = (CreateEmblemWithChosenSubtypeEffect) effect;
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, entry.getControllerId());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        StaticBoostEffect staticEffect = new StaticBoostEffect(
                emblemEffect.powerBoost(),
                emblemEffect.toughnessBoost(),
                emblemEffect.grantedKeywords(),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(chosenSubtype));
        createEmblemEffectHandler.resolve(
                gameData,
                entry,
                new CreateEmblemEffect(List.of(staticEffect), emblemEffect.reminderText()));
    }
}
