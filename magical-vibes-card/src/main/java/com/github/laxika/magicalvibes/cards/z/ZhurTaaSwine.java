package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "210")
public class ZhurTaaSwine extends Card {

    public ZhurTaaSwine() {
        // Bloodrush — {1}{R}{G}, Discard this card: Target attacking creature gets +5/+4 until end of turn.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{R}{G}",
                List.of(new BoostTargetCreatureEffect(5, 4)),
                "Bloodrush — {1}{R}{G}, Discard this card: Target attacking creature gets +5/+4 until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
