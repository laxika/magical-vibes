package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "20")
public class EntreatTheAngels extends Card {

    public EntreatTheAngels() {
        // Miracle {X}{W}{W}
        addCastingOption(new MiracleCast("{X}{W}{W}"));

        // Create X 4/4 white Angel creature tokens with flying.
        addEffect(EffectSlot.SPELL,
                new CreateTokenEffect(new XValue(), "Angel", 4, 4,
                        CardColor.WHITE, List.of(CardSubtype.ANGEL),
                        Set.of(Keyword.FLYING), Set.of()));
    }
}
