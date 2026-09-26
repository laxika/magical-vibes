package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.AdditionalLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "48")
@CardRegistration(set = "LTC", collectorNumber = "131")
public class BilboBirthdayCelebrant extends Card {

    public BilboBirthdayCelebrant() {
        addEffect(EffectSlot.STATIC, new AdditionalLifeGainEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}{B}{G}",
                List.of(
                        new ExileSelfCost(),
                        new SearchLibraryEffect(
                                new Fixed(Integer.MAX_VALUE),
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD)
                ),
                "{2}{W}{B}{G}, {T}, Exile Bilbo: Search your library for any number of creature cards, put them onto the battlefield, then shuffle."
        ).withActivationCondition(
                new ControllerLifeAtLeast(111),
                "Activate only if you have 111 or more life."
        ));
    }
}
