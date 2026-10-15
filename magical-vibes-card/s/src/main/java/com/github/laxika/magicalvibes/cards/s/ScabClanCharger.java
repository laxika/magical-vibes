package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "131")
public class ScabClanCharger extends Card {

    public ScabClanCharger() {
        // Bloodrush — {1}{G}, Discard this card: Target attacking creature gets +2/+4 until end of turn.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new BoostTargetCreatureEffect(2, 4)),
                "Bloodrush — {1}{G}, Discard this card: Target attacking creature gets +2/+4 until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
