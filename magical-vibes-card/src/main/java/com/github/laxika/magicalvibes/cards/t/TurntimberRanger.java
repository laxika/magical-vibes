package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "191")
public class TurntimberRanger extends Card {

    public TurntimberRanger() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.ALLY),
                        new MayEffect(
                                SequenceEffect.of(
                                        new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                                                List.of(CardSubtype.WOLF), Set.of(), Set.of()),
                                        new PutCountersOnSourceEffect(1, 1, 1)),
                                "Create a 2/2 green Wolf creature token?")));
    }
}
