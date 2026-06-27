package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantActivatedAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantActivatedAbilityEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (e.filter() != null
                        && !gameQueryService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                    continue;
                }
                if (e.duration() == EffectDuration.UNTIL_YOUR_NEXT_TURN) {
                    permanent.getUntilNextTurnActivatedAbilities().add(e.ability());
                } else {
                    permanent.getTemporaryActivatedAbilities().add(e.ability());
                }
                count++;
            }
        }

        String durationText = e.duration() == EffectDuration.UNTIL_YOUR_NEXT_TURN
                ? "until your next turn" : "until end of turn";
        String logEntry = entry.getCard().getName() + " grants \"" + e.ability().getDescription()
                + "\" to " + count + " creature(s) " + durationText + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} grants activated ability to {} creature(s) {}",
                gameData.id, entry.getCard().getName(), count, durationText);
    }
}
