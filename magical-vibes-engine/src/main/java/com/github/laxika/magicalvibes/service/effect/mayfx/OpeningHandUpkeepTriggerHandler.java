package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOpeningHandUpkeepTriggerEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Registers the first-upkeep effect of an accepted pregame reveal. */
@Component
@RequiredArgsConstructor
public class OpeningHandUpkeepTriggerHandler implements MayEffectHandlerBean {
    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterOpeningHandUpkeepTriggerEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RegisterOpeningHandUpkeepTriggerEffect effect = ability.effects().stream()
                .filter(RegisterOpeningHandUpkeepTriggerEffect.class::isInstance)
                .map(RegisterOpeningHandUpkeepTriggerEffect.class::cast)
                .findFirst().orElseThrow();
        mayMiscHandlerService.handleOpeningHandUpkeepTrigger(gameData, player, accepted, ability, effect);
    }
}
