package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "114")
public class BattlePlan extends Card {

    public BattlePlan() {
        // At the beginning of combat on your turn, target creature you control gets +2/+0 until
        // end of turn.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new BoostTargetCreatureEffect(2, 0));

        // Basic landcycling {1}{R} ({1}{R}, Discard this card: Search your library for a basic
        // land card, reveal it, put it into your hand, then shuffle.)
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{R} ({1}{R}, Discard this card: Search your library for a "
                        + "basic land card, reveal it, put it into your hand, then shuffle.)"
        ));
    }
}
