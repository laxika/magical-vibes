package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "105")
public class SkinbrandGoblin extends Card {

    public SkinbrandGoblin() {
        // Bloodrush — {R}, Discard this card: Target attacking creature gets +2/+1 until end of turn.
        addHandActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new BoostTargetCreatureEffect(2, 1)),
                "Bloodrush — {R}, Discard this card: Target attacking creature gets +2/+1 until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
