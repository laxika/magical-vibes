package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "CHK", collectorNumber = "136")
public class PainwrackerOni extends Card {

    public PainwrackerOni() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new ControlsPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.OGRE))),
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), SacrificeRecipient.CONTROLLER)
        ));
    }
}
