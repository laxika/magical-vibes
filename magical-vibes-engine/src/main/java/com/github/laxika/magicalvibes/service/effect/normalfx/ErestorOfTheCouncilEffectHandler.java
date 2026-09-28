package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VotingResult;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ErestorOfTheCouncilEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Erestor's snapshot-based voting reward. */
@Component
@RequiredArgsConstructor
public class ErestorOfTheCouncilEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ErestorOfTheCouncilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VotingResult result = ((ErestorOfTheCouncilEffect) effect).votingResult();
        if (result == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        Set<String> controllerChoices = result.choicesByPlayer()
                .getOrDefault(controllerId, Set.of());
        int matchingOpponents = 0;
        int opponentsWithDifferentChoice = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            Set<String> opponentChoices = result.choicesByPlayer().getOrDefault(playerId, Set.of());
            if (opponentChoices.isEmpty()) {
                continue;
            }
            if (opponentChoices.stream().anyMatch(controllerChoices::contains)) {
                matchingOpponents++;
            }
            if (opponentChoices.stream().anyMatch(choice -> !controllerChoices.contains(choice))) {
                opponentsWithDifferentChoice++;
            }
        }

        List<CardEffect> followUps = new ArrayList<>();
        followUps.add(CreateTokenEffect.ofTreasureToken(matchingOpponents));
        followUps.add(new ScryEffect(opponentsWithDifferentChoice));
        followUps.add(new DrawCardEffect());
        entry.insertEffectsToResolve(1, followUps);
    }
}
