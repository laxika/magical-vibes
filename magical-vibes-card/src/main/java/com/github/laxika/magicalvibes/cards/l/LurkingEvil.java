package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "140")
public class LurkingEvil extends Card {

    public LurkingEvil() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        PayLifeCost.halfLife(),
                        new BecomeCreatureEffect(4, 4,
                                List.of(CardSubtype.PHYREXIAN, CardSubtype.HORROR), Set.of(Keyword.FLYING))),
                "Pay half your life, rounded up: This enchantment becomes a 4/4 Phyrexian Horror creature with flying."
        ));
    }
}
