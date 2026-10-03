package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastSpellFromTargetGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Casts the selected instant or sorcery from the targeted opponent's graveyard for free. */
@Component
@RequiredArgsConstructor
public class MayCastSpellFromTargetGraveyardWithoutPayingManaCostHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastSpellFromTargetGraveyardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .anyMatch(effect -> effect instanceof MayCastSpellFromTargetGraveyardWithoutPayingManaCostEffect));
        }
        mayCastHandlerService.handleCastFromGraveyardChoice(
                gameData,
                player,
                accepted,
                ability,
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD,
                        true,
                        true));
    }
}
