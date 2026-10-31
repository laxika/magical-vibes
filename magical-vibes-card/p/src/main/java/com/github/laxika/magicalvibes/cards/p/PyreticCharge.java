package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "29")
@CardRegistration(set = "OTC", collectorNumber = "65")
public class PyreticCharge extends Card {

    public PyreticCharge() {
        addSpellEffect(new DiscardOwnHandThenDrawEffect(new Fixed(4)));
        addSpellEffect(new BoostAllOwnCreaturesEffect(new EventValue(), new Fixed(0)));

        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{R}"))));
    }
}
