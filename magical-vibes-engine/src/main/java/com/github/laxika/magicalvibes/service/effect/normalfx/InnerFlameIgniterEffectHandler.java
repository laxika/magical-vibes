package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.InnerFlameIgniterEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the conditional half of Inner-Flame Igniter's {@code {2}{R}} ability: if this is the
 * third time the ability has resolved this turn, creatures the controller controls gain first strike
 * until end of turn. The unconditional +1/+0 pump is a separate {@code BoostAllOwnCreaturesEffect}.
 *
 * <p>Resolutions are counted per source permanent in {@link GameData#permanentAbilityResolutionsThisTurn}
 * (reset each turn), so the bonus fires on the exact third resolution and not on any later one.
 */
@Component
@RequiredArgsConstructor
public class InnerFlameIgniterEffectHandler implements NormalEffectHandlerBean {

    private static final int FIRST_STRIKE_RESOLUTION = 3;

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return InnerFlameIgniterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        if (selfId == null) {
            return;
        }

        int resolutions = gameData.permanentAbilityResolutionsThisTurn.merge(selfId, 1, Integer::sum);
        if (resolutions != FIRST_STRIKE_RESOLUTION) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                permanent.getGrantedKeywords().add(Keyword.FIRST_STRIKE);
                count++;
            }
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" gives first strike to " + count + " creature(s) until end of turn.").build());
    }
}
