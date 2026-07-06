package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "55")
public class DementiaBat extends Card {

    public DementiaBat() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}",
                List.of(new SacrificeSelfCost(), new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)),
                "{4}{B}, Sacrifice Dementia Bat: Target player discards two cards."
        ));
    }
}
