package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "51")
public class MentalJourney extends Card {

    public MentalJourney() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{U} ({1}{U}, Discard this card: Search your library for a basic land "
                        + "card, reveal it, put it into your hand, then shuffle.)"));
    }
}
