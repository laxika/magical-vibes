package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastInvokeCalamityChosenSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Routes an Invoke Calamity selection to the normal free-cast implementation for its zone. */
@Component
@RequiredArgsConstructor
public class CastInvokeCalamityChosenSpellHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastInvokeCalamityChosenSpellEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastInvokeCalamityChosenSpellEffect effect = ability.effects().stream()
                .filter(CastInvokeCalamityChosenSpellEffect.class::isInstance)
                .map(CastInvokeCalamityChosenSpellEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (effect.fromGraveyard()) {
            mayCastHandlerService.handleCastFromGraveyardChoice(
                    gameData, player, accepted, ability,
                    new CastTargetInstantOrSorceryFromGraveyardEffect(
                            GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true));
        } else {
            mayCastHandlerService.handleMayCastFromHandWithoutPaying(
                    gameData, player, accepted, ability, null, false, false);
        }
    }
}
