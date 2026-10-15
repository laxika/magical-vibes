package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "195")
public class SpiritOfTheAldergard extends Card {

    public SpiritOfTheAldergard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.SNOW),
                        new CardTypePredicate(CardType.LAND)))));
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new PermanentCount(new PermanentHasSupertypePredicate(CardSupertype.SNOW), CountScope.CONTROLLER, true),
                new Fixed(0)));
    }
}
