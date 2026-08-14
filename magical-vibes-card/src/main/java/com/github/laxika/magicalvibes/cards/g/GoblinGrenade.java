package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M12", collectorNumber = "140")
@CardRegistration(set = "ATH", collectorNumber = "32")
public class GoblinGrenade extends Card {

    public GoblinGrenade() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentCost(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                "Sacrifice a Goblin"
        ));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(5, false));
    }
}
