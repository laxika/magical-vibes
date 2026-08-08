package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "38")
public class RubblebeltMaaka extends Card {

    public RubblebeltMaaka() {
        // Bloodrush — {R}, Discard this card: Target attacking creature gets +3/+3 until end of turn.
        addHandActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new BoostTargetCreatureEffect(3, 3)),
                "Bloodrush — {R}, Discard this card: Target attacking creature gets +3/+3 until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
