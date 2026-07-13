package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "155")
public class SengirAutocrat extends Card {

    public SengirAutocrat() {
        // "When this creature enters, create three 0/1 black Serf creature tokens."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(3, "Serf", 0, 1, CardColor.BLACK,
                        List.of(CardSubtype.SERF), Set.of(), Set.of()));

        // "When this creature leaves the battlefield, exile all Serf tokens."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.SERF)
                ))));
    }
}
