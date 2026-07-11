package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "25")
public class StonybrookSchoolmaster extends Card {

    public StonybrookSchoolmaster() {
        // Whenever this creature becomes tapped, you may create a 1/1 blue Merfolk Wizard creature token.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new MayEffect(
                        new CreateTokenEffect("Merfolk Wizard", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.MERFOLK, CardSubtype.WIZARD), Set.of(), Set.of()),
                        "Create a 1/1 blue Merfolk Wizard creature token?")));
    }
}
