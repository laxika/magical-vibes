package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "149")
public class BannerhideKrushok extends Card {

    public BannerhideKrushok() {
        // Trample is auto-loaded from Scryfall.
        // Reinforce 2—{1}{G} ({1}{G}, Discard this card: Put two +1/+1 counters on target creature.)
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Reinforce 2—{1}{G} ({1}{G}, Discard this card: Put two +1/+1 counters on target creature.)",
                TargetFilters.creature()));

        // Scavenge {5}{G}{G}
        addScavenge("{5}{G}{G}");
    }
}
