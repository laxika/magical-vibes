package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "84")
public class KazuulTyrantOfTheCliffs extends Card {

    public KazuulTyrantOfTheCliffs() {
        // Whenever a creature an opponent controls attacks you, create a 3/3 red Ogre creature
        // token unless that creature's controller pays {3}.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU, new MayPayManaEffect(
                "{3}",
                null,
                "Pay {3} to prevent creating an Ogre token?",
                MayPayPayer.TARGET_PERMANENT_CONTROLLER,
                new CreateTokenEffect("Ogre", 3, 3, CardColor.RED, List.of(CardSubtype.OGRE), Set.of(), Set.of()),
                0));
    }
}
