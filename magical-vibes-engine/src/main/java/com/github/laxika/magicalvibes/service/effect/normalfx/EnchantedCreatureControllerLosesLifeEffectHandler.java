package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Makes the enchanted creature's controller lose the life of an {@link EnchantedCreatureControllerLosesLifeEffect}
 * (Stab Wound, Soul Bleed, Contaminated Bond, Ragged Veins, Banewasp Affliction, ...): the baked
 * {@code affectedPlayerId}, or, for a non-targeting trigger, the current (else last-known) controller of the enchanted
 * permanent. The loss goes through {@link LifeSupport#applyLifeLosses}, so it can be replaced (a Lich's Duel Mastery
 * shield or sacrifice), is multiplied, and fires "loses life" triggers.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the life loss is its only change, so a Lich's Duel Mastery sacrifice
 * that replaces it is asked about (CR 616.1) before any life is lost.
 */
@Component
@RequiredArgsConstructor
public class EnchantedCreatureControllerLosesLifeEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedCreatureControllerLosesLifeEffect) effect;
        UUID playerId = e.affectedPlayerId();
        if (entry.isNonTargeting() && entry.getTargetId() != null) {
            UUID currentController = gameQueryService.findPermanentController(gameData, entry.getTargetId());
            if (currentController == null) {
                currentController = entry.getRemovedPermanentControllers().get(entry.getTargetId());
            }
            if (currentController != null) {
                playerId = currentController;
            }
        }
        if (playerId == null || !gameData.playerIds.contains(playerId)) return;

        lifeSupport.applyLifeLosses(gameData, Map.of(playerId, e.amount()), entry.getCard().getName());
    }
}
