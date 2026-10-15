package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreOpponentCreatureHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.PreventOpponentCreatureWardTriggersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "111")
public class NowhereToRun extends Card {

    public NowhereToRun() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostTargetCreatureEffect(-3, -3));
        addEffect(EffectSlot.STATIC, new IgnoreOpponentCreatureHexproofEffect());
        addEffect(EffectSlot.STATIC, new PreventOpponentCreatureWardTriggersEffect());
    }
}
