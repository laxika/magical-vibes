package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "36")
public class TraumaticVisions extends Card {

    public TraumaticVisions() {
        // Counter target spell.
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());

        // Basic landcycling {1}{U} ({1}{U}, Discard this card: Search your library for a basic land
        // card, reveal it, put it into your hand, then shuffle.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{U} ({1}{U}, Discard this card: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.)"));
    }
}
