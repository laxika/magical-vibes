package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

@CardRegistration(set = "BLB", collectorNumber = "44")
public class DaringWaverider extends Card {

    public DaringWaverider() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                true,
                true,
                new CardMaxManaValuePredicate(4)));
    }
}
