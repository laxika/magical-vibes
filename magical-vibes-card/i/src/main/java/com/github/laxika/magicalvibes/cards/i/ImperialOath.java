package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "17")
public class ImperialOath extends Card {

    public ImperialOath() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(3, "Samurai", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.SAMURAI), Set.of(Keyword.VIGILANCE), Set.of()));
        addEffect(EffectSlot.SPELL, new ScryEffect(3));
    }
}
