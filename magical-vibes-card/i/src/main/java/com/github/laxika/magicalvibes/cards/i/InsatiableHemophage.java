package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TimesSourceMutated;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "93")
public class InsatiableHemophage extends Card {

    public InsatiableHemophage() {
        addCastingOption(AlternateHandCast.mutate("{2}{B}", new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)))),
                "Mutate requires a non-Human creature with the spell's owner")));
        addEffect(EffectSlot.ON_SELF_MUTATES, SequenceEffect.of(
                new LoseLifeEffect(new TimesSourceMutated(), LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(new TimesSourceMutated())));
    }
}
