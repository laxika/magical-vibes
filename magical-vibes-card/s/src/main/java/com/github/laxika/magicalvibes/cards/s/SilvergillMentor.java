package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "69")
@CardRegistration(set = "ECL", collectorNumber = "403")
public class SilvergillMentor extends Card {

    public SilvergillMentor() {
        // As an additional cost to cast this spell, behold a Merfolk or pay {2}.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MERFOLK))),
                new IncreaseOwnCastCostUnlessRevealSubtypeEffect(2, CardSubtype.MERFOLK)));

        // When this creature enters, create a 1/1 white and blue Merfolk creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1, "Merfolk", 1, 1, null,
                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.MERFOLK)));
    }
}
