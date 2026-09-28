package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnlyEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MSC", collectorNumber = "661")
public class KingpinWilsonFisk extends Card {

    public KingpinWilsonFisk() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new OncePerTurnTriggerEffect(new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        CreateTokenEffect.ofTreasureToken(2)
                )));
        addEffect(EffectSlot.ON_DEATH,
                new SacrificeOnlyEffect(CreateTokenEffect.ofTreasureToken(2)));
    }
}
