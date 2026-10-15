package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "104")
public class Scorchwalker extends Card {

    public Scorchwalker() {
        // Bloodrush — {1}{R}{R}, Discard this card: Target attacking creature gets +5/+1 until end of turn.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{R}{R}",
                List.of(new BoostTargetCreatureEffect(5, 1)),
                "Bloodrush — {1}{R}{R}, Discard this card: Target attacking creature gets +5/+1 until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
