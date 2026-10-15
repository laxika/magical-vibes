package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "232")
@CardRegistration(set = "TPR", collectorNumber = "174")
public class HeartwoodGiant extends Card {

    public HeartwoodGiant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.FOREST), "Sacrifice a Forest", false),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(2)),
                "{T}, Sacrifice a Forest: Heartwood Giant deals 2 damage to target player or planeswalker."
        ));
    }
}
