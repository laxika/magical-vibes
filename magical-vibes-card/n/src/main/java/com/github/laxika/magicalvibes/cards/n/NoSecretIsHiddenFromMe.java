package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "DSC", collectorNumber = "349")
public class NoSecretIsHiddenFromMe extends Card {

    public NoSecretIsHiddenFromMe() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new ExileTopUntilNonlandMayCastWithoutPayingManaEffect(),
                ConditionalEffect.unless(
                        new ControlsPermanentCount(6, new PermanentIsLandPredicate()),
                        new ExileTopUntilNonlandMayCastWithoutPayingManaEffect())));
    }
}
