package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.WillOfThePlaneswalkersEffect;

@CardRegistration(set = "MOC", collectorNumber = "34")
@CardRegistration(set = "MOC", collectorNumber = "121")
public class PathOfThePyromancer extends Card {

    public PathOfThePyromancer() {
        addSpellEffect(new DiscardHandEffect());
        addSpellEffect(new AwardManaEffect(ManaColor.RED, new EventValue()));
        addSpellEffect(new DrawCardEffect(new Sum(new EventValue(), new Fixed(1))));
        addSpellEffect(new WillOfThePlaneswalkersEffect());
    }
}
