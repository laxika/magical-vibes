package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "FIC", collectorNumber = "67")
@CardRegistration(set = "FIC", collectorNumber = "122")
public class LifestreamsBlessing extends Card {

    public LifestreamsBlessing() {
        addEffect(EffectSlot.SPELL, DrawCardEffect.withCastTimeXValue(
                new GreatestPowerAmongControlled(), new XValue()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastFromZone(Zone.EXILE), new GainLifeEffect(new Scaled(new XValue(), 2))));
        addCastingOption(new ForetellCast("{4}{G}"));
    }
}
