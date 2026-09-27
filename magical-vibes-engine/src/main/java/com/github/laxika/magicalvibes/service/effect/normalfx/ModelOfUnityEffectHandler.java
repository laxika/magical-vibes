package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VotingResult;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayScryEffect;
import com.github.laxika.magicalvibes.model.effect.ModelOfUnityEffect;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Resolves Model of Unity's vote-matching scry reward. */
@Component
public class ModelOfUnityEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ModelOfUnityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VotingResult result = ((ModelOfUnityEffect) effect).votingResult();
        if (result == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Set<String> controllerChoices = result.choicesByPlayer()
                .getOrDefault(controllerId, Set.of());
        List<UUID> eligiblePlayers = EachPlayerMayScryEffectHandler.apnapPlayers(gameData).stream()
                .filter(playerId -> playerId.equals(controllerId)
                        || result.choicesByPlayer().getOrDefault(playerId, Set.of()).stream()
                        .anyMatch(controllerChoices::contains))
                .toList();

        entry.insertEffectsToResolve(1, List.of(new EachPlayerMayScryEffect(2, eligiblePlayers)));
    }
}
