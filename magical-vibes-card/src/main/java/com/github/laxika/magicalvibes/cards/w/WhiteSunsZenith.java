package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "19")
public class WhiteSunsZenith extends Card {

    public WhiteSunsZenith() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new XValue(), "Cat", 2, 2, CardColor.WHITE, List.of(CardSubtype.CAT),
                Set.of(), Set.of()
        ));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
