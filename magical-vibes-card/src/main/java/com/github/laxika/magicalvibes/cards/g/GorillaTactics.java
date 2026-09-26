package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "76")
public class GorillaTactics extends Card {

    public GorillaTactics() {
        CreateTokenEffect gorilla = new CreateTokenEffect(
                "Gorilla", 2, 2, CardColor.GREEN, List.of(CardSubtype.APE), Set.of(), Set.of());
        addEffect(EffectSlot.SPELL, gorilla);
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT, gorilla.withAmount(2));
    }
}
