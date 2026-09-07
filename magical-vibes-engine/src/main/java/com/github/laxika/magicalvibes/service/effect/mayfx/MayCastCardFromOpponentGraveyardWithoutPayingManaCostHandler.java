package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one of Jetsam's per-opponent free graveyard cast choices. */
@Component
@RequiredArgsConstructor
public class MayCastCardFromOpponentGraveyardWithoutPayingManaCostHandler
        implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect effect = ability.effects().stream()
                .filter(MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect.class::isInstance)
                .map(MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted) {
            UUID graveyardOwnerId = effect.graveyardOwnerId();
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .filter(MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect.class::isInstance)
                    .map(MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect.class::cast)
                    .anyMatch(candidate -> graveyardOwnerId.equals(candidate.graveyardOwnerId())));
        }

        mayCastHandlerService.handleCastFromSpecificGraveyardChoice(
                gameData, player, accepted, ability, effect.graveyardOwnerId());
    }
}
