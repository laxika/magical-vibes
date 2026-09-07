package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DistinctManaValuesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "166")
public class AvenHeartstabber extends Card {

    public AvenHeartstabber() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new DistinctManaValuesAmongCardsInGraveyardAtLeast(5),
                new StaticBoostEffect(2, 2, Set.of(Keyword.DEATHTOUCH), GrantScope.SELF)));
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new MillEffect(2, MillRecipient.CONTROLLER),
                new DrawCardEffect(1)));
    }
}
