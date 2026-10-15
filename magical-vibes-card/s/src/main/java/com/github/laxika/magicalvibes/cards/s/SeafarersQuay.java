package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BandsWithOtherEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "306")
public class SeafarersQuay extends Card {

    public SeafarersQuay() {
        PermanentHasSupertypePredicate legendary = new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY);
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new BandsWithOtherEffect(legendary),
                GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                        legendary
                ))));
    }
}
