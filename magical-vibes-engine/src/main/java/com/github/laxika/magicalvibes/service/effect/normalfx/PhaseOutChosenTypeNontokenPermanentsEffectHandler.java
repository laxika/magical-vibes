package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutChosenTypeNontokenPermanentsEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PhaseOutChosenTypeNontokenPermanentsEffect}: pauses so the upkeep player can pick
 * artifact / creature / land / non-Aura enchantment. Completing the choice runs
 * {@link PhaseOutChosenTypeSupport}.
 */
@Component
@RequiredArgsConstructor
public class PhaseOutChosenTypeNontokenPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutChosenTypeNontokenPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID choosingPlayerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        playerInputService.beginTeferisRealmTypeChoice(gameData, choosingPlayerId, entry.getCard());
    }
}
