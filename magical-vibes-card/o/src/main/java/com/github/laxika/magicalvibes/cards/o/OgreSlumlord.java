package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "74")
@CardRegistration(set = "CMM", collectorNumber = "177")
@CardRegistration(set = "CMM", collectorNumber = "518")
@CardRegistration(set = "AFC", collectorNumber = "104")
@CardRegistration(set = "OTC", collectorNumber = "145")
@CardRegistration(set = "BLC", collectorNumber = "186")
@CardRegistration(set = "ZNC", collectorNumber = "50")
public class OgreSlumlord extends Card {

    public OgreSlumlord() {
        // Whenever another nontoken creature dies, you may create a 1/1 black Rat creature token.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER),
                new MayEffect(new CreateTokenEffect("Rat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.RAT), Set.of(), Set.of()),
                        "Create a 1/1 black Rat creature token?"));

        // Rats you control have deathtouch.
        addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.RAT)));
    }
}
