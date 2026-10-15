package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "85")
public class ArcSlogger extends Card {

    public ArcSlogger() {
        // {R}, Exile the top ten cards of your library: This creature deals 2 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new ExileTopCardOfLibraryCost(10), new DealDamageToAnyTargetEffect(2)),
                "{R}, Exile the top ten cards of your library: Arc-Slogger deals 2 damage to any target."
        ));
    }
}
