package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "211")
@CardRegistration(set = "ORI", collectorNumber = "222")
@CardRegistration(set = "INR", collectorNumber = "253")
@CardRegistration(set = "INR", collectorNumber = "438")
public class AngelsTomb extends Card {

    public AngelsTomb() {
        // Whenever a creature you control enters, you may have Angel's Tomb become a 3/3 white
        // Angel artifact creature with flying until end of turn. It stays an artifact.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(
                        new AnimatePermanentsEffect(3, 3,
                                List.of(CardSubtype.ANGEL),
                                Set.of(Keyword.FLYING),
                                CardColor.WHITE),
                        "Have Angel's Tomb become a 3/3 white Angel artifact creature with flying until end of turn?"));
    }
}
