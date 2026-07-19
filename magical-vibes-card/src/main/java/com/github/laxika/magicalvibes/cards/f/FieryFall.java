package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "63")
public class FieryFall extends Card {

    public FieryFall() {
        // Fiery Fall deals 5 damage to target creature.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));

        // Basic landcycling {1}{R} ({1}{R}, Discard this card: Search your library for a basic land
        // card, reveal it, put it into your hand, then shuffle.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{R}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{R} ({1}{R}, Discard this card: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.)"));
    }
}
