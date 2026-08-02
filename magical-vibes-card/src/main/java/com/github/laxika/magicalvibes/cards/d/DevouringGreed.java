package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "110")
public class DevouringGreed extends Card {

    public DevouringGreed() {
        // As an additional cost to cast this spell, you may sacrifice any number of Spirits — the
        // count sacrificed this way snapshots into the spell's X value.
        addEffect(EffectSlot.SPELL, new SacrificeAnyNumberOfPermanentsCost(
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));
        // Target player loses 2 life plus 2 life for each Spirit sacrificed this way.
        // You gain that much life.
        Sum drain = new Sum(new Fixed(2), new Scaled(new XValue(), 2));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(drain, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(drain));
    }
}
