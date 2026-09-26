package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageBecomeMonarchEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "56")
@CardRegistration(set = "LTC", collectorNumber = "139")
public class ForthEorlingas extends Card {

    public ForthEorlingas() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new XValue(), "Human Knight", 2, 2, CardColor.RED,
                List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of()));
        addEffect(EffectSlot.SPELL, new RegisterDelayedCombatDamageBecomeMonarchEffect());
    }
}
