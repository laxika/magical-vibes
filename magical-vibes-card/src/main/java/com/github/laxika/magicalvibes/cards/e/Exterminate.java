package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "68")
public class Exterminate extends Card {

    public Exterminate() {
        addEffect(EffectSlot.SPELL,
                new TapAnyNumberOfPermanentsCost(new PermanentHasSubtypePredicate(CardSubtype.DALEK)));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DestroyTargetPermanentThenEffect(new LoseLifeEffect(3), ThenEffectRecipient.TARGET_CONTROLLER));
        addEffect(EffectSlot.ON_SELF_CAST, ReplicateEffect.forTapCost(
                new PermanentHasSubtypePredicate(CardSubtype.DALEK)));
    }
}
