package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "27")
@CardRegistration(set = "OTC", collectorNumber = "63")
public class ElementalEruption extends Card {

    public ElementalEruption() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Dragon Elemental",
                4,
                4,
                CardColor.RED,
                List.of(CardSubtype.DRAGON, CardSubtype.ELEMENTAL),
                Set.of(Keyword.FLYING, Keyword.PROWESS),
                Set.of()));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
