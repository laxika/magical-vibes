package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesInAttackingBandsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BoostCreaturesInAttackingBandsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturesInAttackingBandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (!entry.getDeclaredTargetIds().isEmpty()) {
            List<Permanent> survivingBand = gameData.playerBattlefields.values().stream()
                    .flatMap(List::stream)
                    .filter(permanent -> entry.getDeclaredTargetIds().contains(permanent.getId()))
                    .filter(permanent -> permanent.isAttacking() && permanent.getBandId() != null)
                    .toList();
            int boost = survivingBand.size();
            for (Permanent permanent : survivingBand) {
                permanent.setPowerModifier(permanent.getPowerModifier() + boost);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + boost);
            }
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " gives creatures in the attacking band +" + boost + "/+" + boost + " until end of turn."));
            return;
        }
        Map<UUID, List<Permanent>> bands = new HashMap<>();
        for (Permanent permanent : battlefield) {
            if (permanent.isAttacking() && permanent.getBandId() != null) {
                bands.computeIfAbsent(permanent.getBandId(), ignored -> new ArrayList<>()).add(permanent);
            }
        }

        int qualifyingBands = 0;
        int boostedCreatures = 0;
        for (List<Permanent> band : bands.values()) {
            if (band.size() < 2) {
                continue;
            }
            int boost = band.size();
            qualifyingBands++;
            for (Permanent permanent : band) {
                permanent.setPowerModifier(permanent.getPowerModifier() + boost);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + boost);
                boostedCreatures++;
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" gives +N/+N to %d creature(s) in %d attacking band(s) until end of turn.",
                        boostedCreatures, qualifyingBands))
                .build());
    }
}
