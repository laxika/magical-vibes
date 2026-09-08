package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesWithPowerAtLeastChosenNumberEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves the chosen-number creature wipe used by Expel the Interlopers. */
@Component
@RequiredArgsConstructor
public class DestroyAllCreaturesWithPowerAtLeastChosenNumberEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final DestroyAllPermanentsEffectHandler destroyAllPermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllCreaturesWithPowerAtLeastChosenNumberEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellNumber == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellNumberChoice(gameData, entry.getControllerId(), 10);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        int chosenNumber = gameData.chosenSpellNumber;
        gameData.chosenSpellNumber = null;

        destroyAllPermanentsEffectHandler.resolve(gameData, entry, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(chosenNumber)))));
    }
}
