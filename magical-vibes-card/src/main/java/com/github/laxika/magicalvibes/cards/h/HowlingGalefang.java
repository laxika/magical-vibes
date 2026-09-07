package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CardsInExileMatchingAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasAdventurePredicate;

import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "175")
public class HowlingGalefang extends Card {

    public HowlingGalefang() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CardsInExileMatchingAtLeast(1, new CardHasAdventurePredicate()),
                new StaticBoostEffect(0, 0, Set.of(Keyword.HASTE), GrantScope.SELF)
        ));
    }
}
