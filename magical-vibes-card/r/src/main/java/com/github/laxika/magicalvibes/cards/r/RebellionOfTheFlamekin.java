package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "188")
public class RebellionOfTheFlamekin extends Card {

    public RebellionOfTheFlamekin() {
        // The clash's outcome is the ability's event value, so exactly one "if" holds as it resolves.
        addTrigger(Triggers.youClash(), new ConditionalEffect(new EventValueAtLeast(1),
                        new MayPayManaEffect("{1}", createTokenEffect(Set.of(Keyword.HASTE)),
                                "Pay {1} to create a 3/1 red Elemental Shaman with haste?")),
                new ConditionalEffect(new NotCondition(new EventValueAtLeast(1)),
                        new MayPayManaEffect("{1}", createTokenEffect(Set.of()),
                                "Pay {1} to create a 3/1 red Elemental Shaman?")));
    }

    private static CreateTokenEffect createTokenEffect(Set<Keyword> grantedKeywordsUntilEndOfTurn) {
        return new CreateTokenEffect(1, "Elemental Shaman", 3, 1, CardColor.RED, null,
                List.of(CardSubtype.ELEMENTAL, CardSubtype.SHAMAN), Set.of(), grantedKeywordsUntilEndOfTurn);
    }
}
