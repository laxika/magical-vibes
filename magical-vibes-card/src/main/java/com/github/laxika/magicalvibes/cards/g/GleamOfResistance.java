package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "8")
public class GleamOfResistance extends Card {

    public GleamOfResistance() {
        // Creatures you control get +1/+2 until end of turn. Untap those creatures.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 2));
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));

        // Basic landcycling {1}{W} ({1}{W}, Discard this card: Search your library for a basic land
        // card, reveal it, put it into your hand, then shuffle.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{W} ({1}{W}, Discard this card: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.)"));
    }
}
