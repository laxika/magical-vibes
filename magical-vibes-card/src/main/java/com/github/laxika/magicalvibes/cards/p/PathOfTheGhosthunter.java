package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.WillOfThePlaneswalkersEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "18")
@CardRegistration(set = "MOC", collectorNumber = "105")
public class PathOfTheGhosthunter extends Card {

    public PathOfTheGhosthunter() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(new XValue(), "Spirit", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.SPELL, new WillOfThePlaneswalkersEffect());
    }
}
