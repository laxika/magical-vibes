package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "177")
public class EarthenAlly extends Card {

    public EarthenAlly() {
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new ColorsAmongControlledPermanents(new PermanentHasSubtypePredicate(CardSubtype.ALLY)),
                new Fixed(0)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{U}{B}{R}{G}",
                List.of(new EarthbendTargetLandEffect(5)),
                "Earthbend 5."));
    }
}
