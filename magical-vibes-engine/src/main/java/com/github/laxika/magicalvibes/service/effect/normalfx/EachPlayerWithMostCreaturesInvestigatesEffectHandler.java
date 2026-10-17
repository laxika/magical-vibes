package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerWithMostCreaturesInvestigatesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerWithMostCreaturesInvestigatesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerWithMostCreaturesInvestigatesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int mostCreatures = -1;
        List<UUID> playersWithMostCreatures = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            int creatureCount = battlefield == null ? 0 : (int) battlefield.stream()
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                    .count();
            if (creatureCount > mostCreatures) {
                mostCreatures = creatureCount;
                playersWithMostCreatures.clear();
                playersWithMostCreatures.add(playerId);
            } else if (creatureCount == mostCreatures) {
                playersWithMostCreatures.add(playerId);
            }
        }

        // A token-creation replacement offer parks the creation and re-runs this effect once the choice is made.
        // Resume at the player the offer belongs to so players who already investigated don't repeat it.
        int startIndex = 0;
        if (gameData.pendingTokenCreationReplacement != null) {
            UUID parkedController = gameQueryService.findPermanentController(
                    gameData, gameData.pendingTokenCreationReplacement.replacementPermanentId());
            int parkedIndex = playersWithMostCreatures.indexOf(parkedController);
            if (parkedIndex > 0) {
                startIndex = parkedIndex;
            }
        }
        for (UUID playerId : playersWithMostCreatures.subList(startIndex, playersWithMostCreatures.size())) {
            createTokenEffectHandler.resolveForController(
                    gameData, entry, CreateTokenEffect.ofClueToken(1), playerId);
            if (gameData.pendingTokenCreationReplacement != null) {
                return;
            }
        }
    }
}
