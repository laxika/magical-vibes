package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensWithSameNameAsLeavingCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves Dual Nature's nontoken-creature leaves trigger. */
@Component
@RequiredArgsConstructor
public class ExileTokensWithSameNameAsLeavingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTokensWithSameNameAsLeavingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String creatureName = ((ExileTokensWithSameNameAsLeavingCreatureEffect) effect).creatureName();
        if (creatureName == null) {
            return;
        }

        List<Permanent> tokens = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && creatureName.equals(permanent.getCard().getName()))
                .forEach(tokens::add));

        for (Permanent token : tokens) {
            if (permanentRemovalService.removePermanentToExile(gameData, token)) {
                gameLogService.append(gameData, GameLog.cardThen(token.getCard(), " is exiled."));
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
