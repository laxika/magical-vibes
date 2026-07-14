package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "3")
public class CennsEnlistment extends Card {

    public CennsEnlistment() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Kithkin Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER), Set.of(), Set.of()));
        addCastingOption(new Retrace());
    }
}
