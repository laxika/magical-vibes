package com.github.laxika.magicalvibes.cards.t;

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
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "36")
public class TrainedArynx extends Card {

    public TrainedArynx() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceIsSaddled(), SequenceEffect.of(
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF),
                        new ScryEffect(1))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(2), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 2",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
