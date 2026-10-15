package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "1")
public class ArmamentMaster extends Card {

    public ArmamentMaster() {
        Scaled boost = new Scaled(new AttachmentsOnSource(false, true), 2);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                boost,
                boost,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.KOR)));
    }
}
