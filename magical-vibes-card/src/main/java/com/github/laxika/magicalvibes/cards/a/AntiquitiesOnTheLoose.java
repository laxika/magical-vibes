package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "7")
public class AntiquitiesOnTheLoose extends Card {

    public AntiquitiesOnTheLoose() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Spirit", 2, 2,
                CardColor.WHITE,
                Set.of(CardColor.RED, CardColor.WHITE),
                List.of(CardSubtype.SPIRIT)
        ));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastNotFromHand(), 
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)
                )
        ));
        addCastingOption(new FlashbackCast("{4}{W}{W}"));
    }
}
