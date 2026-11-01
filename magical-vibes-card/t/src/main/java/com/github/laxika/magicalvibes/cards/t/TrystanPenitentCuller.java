package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenEachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class TrystanPenitentCuller extends Card {

    public TrystanPenitentCuller() {
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS), SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new ExileOwnGraveyardCardThenEachOpponentLosesLifeEffect(
                        new CardSubtypePredicate(CardSubtype.ELF), 2)));
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{G}", new TransformSelfEffect(),
                        "Pay {G} to transform Trystan?"));
    }
}
