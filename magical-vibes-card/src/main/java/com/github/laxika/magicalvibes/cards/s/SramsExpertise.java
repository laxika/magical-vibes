package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "24")
public class SramsExpertise extends Card {

    public SramsExpertise() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(3, "Servo", 1, 1, null,
                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT)));
        addEffect(EffectSlot.SPELL, new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                new CardMaxManaValuePredicate(3)));
    }
}
