package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPM", collectorNumber = "24")
@CardRegistration(set = "SPM", collectorNumber = "248")
public class WithGreatPower extends Card {

    public WithGreatPower() {
        Scaled twicePerAttachment = new Scaled(new AttachmentsOnSource(true, true), 2);
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        twicePerAttachment, twicePerAttachment, GrantScope.ENCHANTED_CREATURE, true))
                .addEffect(EffectSlot.STATIC, new RedirectPlayerDamageToEnchantedCreatureEffect());
    }
}
