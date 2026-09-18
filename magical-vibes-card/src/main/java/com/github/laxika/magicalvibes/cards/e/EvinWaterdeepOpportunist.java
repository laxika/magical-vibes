package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SLX", collectorNumber = "26")
public class EvinWaterdeepOpportunist extends Card {

    public EvinWaterdeepOpportunist() {
        PermanentCount treasures = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.TREASURE), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC,
                new BoostSelfEffect(new Scaled(treasures, 2), new Fixed(0)));
        addEffect(EffectSlot.ON_ANY_CREATURE_SACRIFICED,
                new OncePerTurnTriggerEffect(CreateTokenEffect.ofTreasureToken(1, true)));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessSacrificesEffect(new PermanentIsCreaturePredicate(), "creature"));
    }
}
