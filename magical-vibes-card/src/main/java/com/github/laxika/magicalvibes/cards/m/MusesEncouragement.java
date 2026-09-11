package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "61")
public class MusesEncouragement extends Card {

    public MusesEncouragement() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(1, "Elemental", 3, 3,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.SPELL, new SurveilEffect(2));
    }
}
