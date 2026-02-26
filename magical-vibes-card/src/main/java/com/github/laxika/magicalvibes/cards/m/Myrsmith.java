package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOnOwnSpellCastWithCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "16")
public class Myrsmith extends Card {

    public Myrsmith() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new CreateTokenOnOwnSpellCastWithCostEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        1,
                        new CreateCreatureTokenEffect("Myr", 1, 1, null,
                                List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "Pay {1} to create a 1/1 colorless Myr artifact creature token?"
        ));
    }
}
