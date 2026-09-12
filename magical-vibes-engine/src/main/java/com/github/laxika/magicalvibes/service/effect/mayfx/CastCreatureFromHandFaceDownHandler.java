package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCreatureFromHandFaceDownEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Accept/decline handler for Illusionary Mask's face-down creature offers. */
@Component
@RequiredArgsConstructor
public class CastCreatureFromHandFaceDownHandler implements MayEffectHandlerBean {

    private final SpellCastingService spellCastingService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastCreatureFromHandFaceDownEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastCreatureFromHandFaceDownEffect effect = ability.effects().stream()
                .filter(CastCreatureFromHandFaceDownEffect.class::isInstance)
                .map(CastCreatureFromHandFaceDownEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted) {
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .filter(CastCreatureFromHandFaceDownEffect.class::isInstance)
                    .map(CastCreatureFromHandFaceDownEffect.class::cast)
                    .anyMatch(candidate -> effect.offerGroupId() != null
                            && effect.offerGroupId().equals(candidate.offerGroupId())));
            spellCastingService.castCreatureFromHandFaceDown(gameData, player, ability.sourceCard());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
