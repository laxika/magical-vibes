package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

@CardRegistration(set = "TLA", collectorNumber = "102")
public class FoggySwampVisions extends Card {

    public FoggySwampVisions() {
        addCastingCost(new ChooseXValueCost(0, Integer.MAX_VALUE));
        addCastingCost(WaterbendCost.x());
        addSpellEffect(new ExileCreaturesFromGraveyardAndCreateTokensEffect(
                false, true, null, null, null, null, GraveyardSearchScope.ALL_GRAVEYARDS));
        addSpellEffect(new SacrificeCreatedPermanentsAtEndStepEffect(true));
    }
}
