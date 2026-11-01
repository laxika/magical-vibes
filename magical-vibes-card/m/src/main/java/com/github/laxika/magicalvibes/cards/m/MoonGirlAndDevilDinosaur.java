package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "223")
public class MoonGirlAndDevilDinosaur extends Card {

    public MoonGirlAndDevilDinosaur() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, SequenceEffect.of(
                new SetBasePowerToughnessEffect(6, 6, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new OncePerTurnTriggerEffect(new DrawCardEffect()));
    }
}
