package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "MSC", collectorNumber = "64")
@CardRegistration(set = "MSC", collectorNumber = "376")
public class AncestralCommunion extends Card {

    public AncestralCommunion() {
        addEffect(EffectSlot.ON_SELF_CAST,
                CopyThisSpellIfConditionEffect.whenCastWhile(new ControlledCommanderAsCast(), true));
        addEffect(EffectSlot.SPELL,
                ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(new CardIsPermanentPredicate()));
    }
}
