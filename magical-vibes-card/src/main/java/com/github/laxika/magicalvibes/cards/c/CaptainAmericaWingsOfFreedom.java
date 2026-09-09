package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "10")
public class CaptainAmericaWingsOfFreedom extends Card {

    public CaptainAmericaWingsOfFreedom() {
        var otherHeroes = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.HERO),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                new SourceToughness(), new SourceToughness(), otherHeroes
        ));
    }
}
