package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEqualToLifeTotalAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrLoseGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "2ED", collectorNumber = "114")
@CardRegistration(set = "ME4", collectorNumber = "89")
@CardRegistration(set = "LEA", collectorNumber = "113")
@CardRegistration(set = "LEB", collectorNumber = "114")
public class Lich extends Card {

    public Lich() {
        addStaticEffect(new LoseLifeEqualToLifeTotalAsEntersEffect());
        addStaticEffect(new CantLoseGameFromLifeEffect());
        addStaticEffect(new NefariousLichLifeGainReplacementEffect());
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE, new SacrificePermanentsOrLoseGameEffect(
                new EventValue(), new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS), new ControllerLosesGameEffect());
    }
}
