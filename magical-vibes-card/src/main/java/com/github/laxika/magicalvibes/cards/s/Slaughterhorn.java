package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "134")
public class Slaughterhorn extends Card {

    public Slaughterhorn() {
        // Bloodrush — {G}, Discard this card: Target attacking creature gets +3/+2 until end of turn.
        addHandActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new BoostTargetCreatureEffect(3, 2)),
                "Bloodrush — {G}, Discard this card: Target attacking creature gets +3/+2 until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
