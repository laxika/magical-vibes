package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "176")
public class FlyingCraneTechnique extends Card {

    public FlyingCraneTechnique() {
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Set.of(Keyword.FLYING, Keyword.DOUBLE_STRIKE), GrantScope.OWN_CREATURES));
    }
}
