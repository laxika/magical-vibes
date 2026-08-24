package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WAR", collectorNumber = "27")
public class RallyOfWings extends Card {

    public RallyOfWings() {
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(
                TapUntapScope.CONTROLLED,
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(
                2,
                2,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
