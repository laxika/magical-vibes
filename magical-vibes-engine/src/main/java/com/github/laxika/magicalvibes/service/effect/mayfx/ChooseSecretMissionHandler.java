package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SecretMissionEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Starts a Secret Mission's pregame choice. The choice is not optional, so declining the offer asks for
 * the mission anyway.
 */
@Component
@RequiredArgsConstructor
public class ChooseSecretMissionHandler implements MayEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SecretMissionEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        SecretMissionEffect effect = ability.effects().stream()
                .filter(SecretMissionEffect.class::isInstance)
                .map(SecretMissionEffect.class::cast)
                .findFirst()
                .orElseThrow();
        playerInputService.beginSecretMissionChoice(gameData, ability.controllerId(), ability.sourceCard(),
                effect.missions().stream().map(SecretMissionEffect.Mission::label).toList());
    }
}
