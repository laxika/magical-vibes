package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastMilledSpellWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cast one of the just-milled instant or sorcery cards for free (Jace's Mindseeker). Reuses the
 * cast-from-graveyard routine; accepting one offer clears the sibling offers so only a single spell
 * is cast, while declining leaves the remaining milled cards on offer.
 */
@Component
@RequiredArgsConstructor
public class MayCastMilledSpellWithoutPayingHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastMilledSpellWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            gameData.pendingMayAbilities.removeIf(pma -> pma.effects().stream()
                    .anyMatch(e -> e instanceof MayCastMilledSpellWithoutPayingManaCostEffect));
        }
        mayCastHandlerService.handleCastFromGraveyardChoice(gameData, player, accepted, ability,
                new CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope.OPPONENT_GRAVEYARD, true));
    }
}
