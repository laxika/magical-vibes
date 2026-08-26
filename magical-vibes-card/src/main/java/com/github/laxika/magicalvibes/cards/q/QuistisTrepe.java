package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;

@CardRegistration(set = "FIN", collectorNumber = "66")
public class QuistisTrepe extends Card {

    public QuistisTrepe() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.ALL_GRAVEYARDS, false, true, true));
    }
}
