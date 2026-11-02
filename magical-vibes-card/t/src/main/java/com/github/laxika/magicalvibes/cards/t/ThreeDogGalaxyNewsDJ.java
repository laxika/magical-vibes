package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSacrificedAuraAttachedToOtherAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "120")
@CardRegistration(set = "PIP", collectorNumber = "430")
@CardRegistration(set = "PIP", collectorNumber = "648")
@CardRegistration(set = "PIP", collectorNumber = "958")
public class ThreeDogGalaxyNewsDJ extends Card {

    public ThreeDogGalaxyNewsDJ() {
        addTrigger(Triggers.youAttack(),
                new MayPayManaAndSacrificePermanentEffect(
                        "{2}",
                        new PermanentIsAuraAttachedToSourcePredicate(),
                        new CreateTokenCopiesOfSacrificedAuraAttachedToOtherAttackingCreaturesEffect(),
                        "an Aura attached to Three Dog"));
    }
}
