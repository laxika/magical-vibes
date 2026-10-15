package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "13")
public class MastersCall extends Card {

    public MastersCall() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Myr", 1, 1, null, List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT)));
    }
}
