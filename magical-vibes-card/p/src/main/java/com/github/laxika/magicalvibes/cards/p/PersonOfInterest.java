package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "139")
public class PersonOfInterest extends Card {

    public PersonOfInterest() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SuspectEffect(GrantScope.SELF));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1, "Detective", 2, 2, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.DETECTIVE)));
    }
}
