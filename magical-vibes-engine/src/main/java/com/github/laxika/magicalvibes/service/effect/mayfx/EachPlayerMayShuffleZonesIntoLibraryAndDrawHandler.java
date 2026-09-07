package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerMayShuffleZonesIntoLibraryAndDrawEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies each player's choice after all players have made it. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayShuffleZonesIntoLibraryAndDrawHandler implements MayEffectHandlerBean {

    private final EachPlayerMayShuffleZonesIntoLibraryAndDrawEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect effect =
                (EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect) ability.effects().getFirst();
        List<UUID> remaining = effect.remainingPlayerIds().subList(1, effect.remainingPlayerIds().size());
        List<UUID> acceptedPlayerIds = new ArrayList<>(effect.acceptedPlayerIds());
        if (accepted) {
            acceptedPlayerIds.add(player.getId());
        }

        if (remaining.isEmpty()) {
            effectHandler.resolveAcceptedPlayers(gameData, acceptedPlayerIds, effect.cardsToDraw());
        } else {
            effectHandler.promptNext(gameData, ability.sourceCard(),
                    new EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect(
                            effect.cardsToDraw(), remaining, acceptedPlayerIds));
        }

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
