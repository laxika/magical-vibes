package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "178")
public class StensiaUprising extends Card {

    public StensiaUprising() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new CreateTokenEffect("Human", 1, 1, CardColor.RED,
                        List.of(CardSubtype.HUMAN), Set.of(), Set.of()),
                ConditionalEffect.unless(
                        new AllOf(List.of(
                                new ControlsPermanentCount(13, new PermanentTruePredicate()),
                                new ControlsPermanentCountAtMost(13, new PermanentTruePredicate()))),
                        new MayEffect(
                                new SacrificePermanentThenEffect(
                                        new PermanentIsSourcePermanentPredicate(),
                                        new DealDamageToAnyTargetEffect(7),
                                        "this enchantment"),
                                "Sacrifice this enchantment?"))));
    }
}
