package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "15")
public class PhyrexianRebirth extends Card {

    public PhyrexianRebirth() {
        // Destroy all creatures, then create an X/X colorless Phyrexian Horror artifact creature
        // token, where X is the number of creatures destroyed this way.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(),
                new CreateTokenEffect(
                        "Phyrexian Horror",
                        new EventValue(),
                        new EventValue(),
                        null,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.HORROR),
                        Set.of(),
                        Set.of(CardType.ARTIFACT))));
    }
}
