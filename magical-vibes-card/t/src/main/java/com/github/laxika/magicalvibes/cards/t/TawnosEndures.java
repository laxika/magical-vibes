package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInExile;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YBRO", collectorNumber = "3")
public class TawnosEndures extends Card {

    public TawnosEndures() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL,
                        new PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
                                EffectSlot.EXILED_UPKEEP_TRIGGERED,
                                new ConditionalEffect(new SourceCardInExile(), SequenceEffect.of(
                                        new PerpetuallyBoostSourceEffect(1, 1),
                                        new MayEffect(new ReturnSourceCardFromExileToBattlefieldEffect(false),
                                                "Return it to the battlefield?")))));
    }
}
