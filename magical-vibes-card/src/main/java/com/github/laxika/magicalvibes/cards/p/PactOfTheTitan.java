package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterPayManaOrLoseGameAtNextUpkeepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "103")
public class PactOfTheTitan extends Card {

    public PactOfTheTitan() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Giant", 4, 4, CardColor.RED, List.of(CardSubtype.GIANT), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new RegisterPayManaOrLoseGameAtNextUpkeepEffect("{4}{R}"));
    }
}
