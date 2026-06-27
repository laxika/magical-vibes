package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegenerateAllOwnCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegenerateAllOwnCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegenerateAllOwnCreaturesEffect) effect;
        
                List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
                if (battlefield == null) return;

                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                        .withSourceControllerId(entry.getControllerId());

                int count = 0;
                for (Permanent perm : battlefield) {
                    if (gameQueryService.isCreature(gameData, perm)
                            && (e.filter() == null
                                || gameQueryService.matchesPermanentPredicate(perm, e.filter(), filterContext))) {
                        perm.setRegenerationShield(perm.getRegenerationShield() + 1);
                        count++;
                    }
                }

                if (count > 0) {
                    String logEntry = count + " creature(s) gain a regeneration shield.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} creature(s) gain regeneration shields", gameData.id, count);
                }
    
    }
}
