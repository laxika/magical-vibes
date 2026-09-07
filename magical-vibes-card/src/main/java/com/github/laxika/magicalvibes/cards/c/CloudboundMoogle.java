package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "11")
public class CloudboundMoogle extends Card {

    public CloudboundMoogle() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAINS))),
                "Plainscycling {2} ({2}, Discard this card: Search your library for a Plains card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
