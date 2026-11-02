package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "141")
@CardRegistration(set = "HBG", collectorNumber = "180")
public class EarthCultElemental extends Card {

    public EarthCultElemental() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RollD20Effect(
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(),
                        SacrificeRecipient.EACH_PLAYER),
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(),
                        SacrificeRecipient.EACH_OPPONENT),
                new SacrificePermanentsEffect(2, new PermanentTruePredicate(),
                        SacrificeRecipient.EACH_OPPONENT)));
    }
}
