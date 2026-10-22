package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "34")
public class UnswervingSloth extends Card {

    public UnswervingSloth() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceIsSaddled(), SequenceEffect.of(
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(4), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 4",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
