package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTargetPlayerTopCardMayGraveyardEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Eye Spy — may put target player's looked-at top card into their graveyard.
 */
@Component
@RequiredArgsConstructor
public class LookAtTargetPlayerTopCardMayGraveyardHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTargetPlayerTopCardMayGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTargetPlayerTopCardMayGraveyardEffect eyeSpy = ability.effects().stream()
                .filter(e -> e instanceof LookAtTargetPlayerTopCardMayGraveyardEffect)
                .map(e -> (LookAtTargetPlayerTopCardMayGraveyardEffect) e)
                .findFirst().orElse(null);
        if (eyeSpy != null) {
            mayMiscHandlerService.handleLookAtTargetPlayerTopCardChoice(gameData, accepted,
                    eyeSpy.libraryOwnerId(), player.getId(), eyeSpy.lifeCost());
        }
    }
}
