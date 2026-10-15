package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "82")
public class CausticBronco extends Card {

    public CausticBronco() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalReplacementEffect(
                new SourceIsSaddled(),
                new RevealTopCardPutIntoHandAndChangeLifeEffect(false),
                new RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(3), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 3",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
