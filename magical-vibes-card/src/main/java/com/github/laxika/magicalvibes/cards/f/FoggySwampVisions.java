package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

@CardRegistration(set = "TLA", collectorNumber = "102")
public class FoggySwampVisions extends Card {

    public FoggySwampVisions() {
        addEffect(EffectSlot.SPELL, new ChooseXValueCost(0, 100));
        addEffect(EffectSlot.SPELL, WaterbendCost.x());
        addEffect(EffectSlot.SPELL, new ExileCreaturesFromGraveyardAndCreateTokensEffect(
                false, true, null, null, null, null, GraveyardSearchScope.ALL_GRAVEYARDS));
        addEffect(EffectSlot.SPELL, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
