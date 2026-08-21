package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RNA", collectorNumber = "18")
public class RallyToBattle extends Card {

    public RallyToBattle() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 3));
        addEffect(EffectSlot.SPELL,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));
    }
}
