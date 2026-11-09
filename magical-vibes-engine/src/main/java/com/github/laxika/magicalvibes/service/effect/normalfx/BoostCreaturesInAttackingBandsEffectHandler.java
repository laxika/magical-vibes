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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        // The band is the entry's attacking creatures, recorded as the ability triggered for that band (EACH_BAND);
        // the ones still attacking in a band as it resolves are counted and boosted.
        Set<UUID> bandIds = entry.getAttackingPermanentSnapshots().stream()
                .map(Permanent::getId)
                .collect(Collectors.toSet());
        List<Permanent> survivingBand = gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> bandIds.contains(permanent.getId()))
                .filter(permanent -> permanent.isAttacking() && permanent.getBandId() != null)
                .toList();
        int boost = survivingBand.size();
        for (Permanent permanent : survivingBand) {
            permanent.setPowerModifier(permanent.getPowerModifier() + boost);
            permanent.setToughnessModifier(permanent.getToughnessModifier() + boost);
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " gives creatures in the attacking band +" + boost + "/+" + boost + " until end of turn."));
    }
}
