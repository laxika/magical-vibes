package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "94")
public class SylvanBounty extends Card {

    public SylvanBounty() {
        // Target player gains 8 life.
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(8));

        // Basic landcycling {1}{G} ({1}{G}, Discard this card: Search your library for a basic land
        // card, reveal it, put it into your hand, then shuffle.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{G} ({1}{G}, Discard this card: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.)"));
    }
}
