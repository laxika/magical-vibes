package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TakeInitiativeEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves taking the initiative. A player taking it triggers the initiative's "whenever a player takes the
 * initiative, that player ventures into Undercity", which has no source and is controlled by the player who has the
 * initiative as it triggers (CR 727.2).
 */
@Component
@RequiredArgsConstructor
public class TakeInitiativeEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TakeInitiativeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = ((TakeInitiativeEffect) effect).thatPlayer() && entry.getTargetId() != null
                ? entry.getTargetId() : entry.getControllerId();
        gameData.initiativePlayerId = playerId;
        triggeredAbilityStackService.putInherent(gameData, playerId, "The initiative's ability",
                List.of(new VentureIntoDungeonEffect(Dungeon.UNDERCITY)), venture -> venture.setNonTargeting(true));
    }
}
