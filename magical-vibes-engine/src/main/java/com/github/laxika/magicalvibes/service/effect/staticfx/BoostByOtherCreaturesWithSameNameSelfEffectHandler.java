package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostByOtherCreaturesWithSameNameSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostByOtherCreaturesWithSameNameEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostByOtherCreaturesWithSameNameEffect) effect;
        if (boost.scope() != null) {
            return;
        }
        String sourceName = sameNameCheckName(context.source());
        if (sourceName == null) {
            return;
        }
        GameData gameData = context.gameData();
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(gameData);

        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getId().equals(context.sourceId())) return;
            if (boost.onlyControlled() && !playerId.equals(context.sourceControllerId())) return;
            if (!support.isEffectivelyCreature(permanent, hasAnimateArtifacts)) return;
            if (!sourceName.equals(sameNameCheckName(permanent))) return;
            count[0]++;
        });

        accumulator.addPower(count[0] * boost.powerPerCreature());
        accumulator.addToughness(count[0] * boost.toughnessPerCreature());
    }

    static String sameNameCheckName(Permanent permanent) {
        if (permanent.isFaceDown()) {
            return null;
        }
        CharacteristicState activeState = LayerSystemService.activeStateFor(permanent.getId());
        return activeState != null && activeState.getName() != null
                ? activeState.getName()
                : permanent.getCard().getName();
    }
}
