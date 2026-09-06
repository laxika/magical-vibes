package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "6")
public class Aurification extends Card {

    public Aurification() {
        var goldCounterBearer = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasCountersPredicate(CounterType.GOLD)));

        addEffect(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.GOLD));
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.WALL, GrantScope.ALL_CREATURES_INCLUDING_SELF, false,
                        goldCounterBearer));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                        goldCounterBearer));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new RemoveAllCountersFromMatchingPermanentsEffect(CounterType.GOLD,
                        new PermanentIsCreaturePredicate()));
    }
}
