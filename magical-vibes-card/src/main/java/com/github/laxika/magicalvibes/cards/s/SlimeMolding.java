package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "135")
public class SlimeMolding extends Card {

    public SlimeMolding() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Ooze", new XValue(), new XValue(), CardColor.GREEN, List.of(CardSubtype.OOZE),
                Set.of(), Set.of()
        ));
    }
}
