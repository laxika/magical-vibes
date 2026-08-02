package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "74")
public class OgreSlumlord extends Card {

    public OgreSlumlord() {
        // Whenever another nontoken creature dies, you may create a 1/1 black Rat creature token.
        // The dying permanent has already left the battlefield when the slot is dispatched, so this
        // creature's own death never triggers it ("another").
        addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                new MayEffect(new CreateTokenEffect("Rat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.RAT), Set.of(), Set.of()),
                        "Create a 1/1 black Rat creature token?"));

        // Rats you control have deathtouch.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.RAT)));
    }
}
