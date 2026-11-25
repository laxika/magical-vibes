package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSacrificedAuraAttachedToOtherAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "120")
@CardRegistration(set = "PIP", collectorNumber = "430")
@CardRegistration(set = "PIP", collectorNumber = "648")
@CardRegistration(set = "PIP", collectorNumber = "958")
public class ThreeDogGalaxyNewsDJ extends Card {

    public ThreeDogGalaxyNewsDJ() {
        addTrigger(Triggers.youAttack(),
                // "When you sacrifice an Aura this way" is a reflexive trigger (CR 603.12).
                UnlessPaysEffect.youMayPay(AllOfCost.of(new PayManaCost("{2}"), new SacrificePermanentCost(
                                new PermanentIsAuraAttachedToSourcePredicate(), "Aura attached to Three Dog", false)),
                        new QueueReflexiveAbilityEffect(
                                new CreateTokenCopiesOfSacrificedAuraAttachedToOtherAttackingCreaturesEffect())));
    }
}
