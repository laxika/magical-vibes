package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "12")
public class CrusadingKnight extends Card {

    public CrusadingKnight() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));

        PermanentCount opponentSwamps =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.OPPONENTS);
        addStaticEffect(new BoostSelfEffect(opponentSwamps, opponentSwamps));
    }
}
