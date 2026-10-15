package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "C16", collectorNumber = "21")
public class BenefactorsDraught extends Card {

    public BenefactorsDraught() {
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES));
        addEffect(EffectSlot.SPELL, new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                EffectSlot.ON_ANY_CREATURE_BLOCKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new DrawCardEffect())));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
