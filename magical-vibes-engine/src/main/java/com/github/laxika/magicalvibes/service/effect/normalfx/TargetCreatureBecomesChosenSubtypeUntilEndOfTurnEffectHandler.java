package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, entry.getControllerId(), Set.of(CardSubtype.WALL));
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        var chosenEffect = (TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect) effect;
        if (chosenEffect.scope() == GrantScope.ALL_CREATURES) {
            int[] affectedCount = {0};
            gameData.forEachBattlefield((playerId, battlefield) -> {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.isCreature(gameData, permanent)) {
                        permanent.setTransientCreatureTypeOverride(chosenSubtype);
                        affectedCount[0]++;
                    }
                }
            });
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" makes " + affectedCount[0] + " creature(s) into "
                            + chosenSubtype.getDisplayName() + "s until end of turn.")
                    .build());
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setTransientCreatureTypeOverride(chosenSubtype);
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" becomes a " + chosenSubtype.getDisplayName() + " until end of turn.")
                .build());
    }
}
