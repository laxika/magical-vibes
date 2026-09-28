package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueHandler
        implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .anyMatch(MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect.class::isInstance));
        }
        mayCastHandlerService.handleMayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValue(
                gameData, player, accepted, ability);
    }
}
