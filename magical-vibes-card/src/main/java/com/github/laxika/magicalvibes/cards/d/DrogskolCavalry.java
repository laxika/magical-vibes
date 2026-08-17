package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "15")
public class DrogskolCavalry extends Card {

    public DrogskolCavalry() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.SPIRIT),
                new GainLifeEffect(2)
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(CreateTokenEffect.whiteSpirit(1)),
                "{3}{W}: Create a 1/1 white Spirit creature token with flying."
        ));
    }
}
