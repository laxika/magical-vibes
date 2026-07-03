package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BounceCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BounceCreatureOnUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BounceCreatureOnUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var bounceEffect = (BounceCreatureOnUpkeepEffect) effect;
        UUID choosingPlayerId = switch (bounceEffect.scope()) {
            case SOURCE_CONTROLLER -> entry.getControllerId();
            case TRIGGER_TARGET_PLAYER -> entry.getTargetId() != null
                    ? entry.getTargetId()
                    : entry.getControllerId();
        };
        String playerName = gameData.playerIdToName.get(choosingPlayerId);

        List<Permanent> battlefield = gameData.playerBattlefields.get(choosingPlayerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)
                        && predicateEvaluationService.matchesFilters(
                        p,
                        bounceEffect.filters(),
                        FilterContext.of(gameData)
                                .withSourceCardId(entry.getCard().getId())
                                .withSourceControllerId(entry.getControllerId()))) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String logEntry = playerName + " controls no valid creatures — nothing to return.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(choosingPlayerId));
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId, creatureIds, bounceEffect.prompt());
    }
}
