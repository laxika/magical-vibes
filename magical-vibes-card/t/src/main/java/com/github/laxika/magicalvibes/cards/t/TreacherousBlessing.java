package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "117")
@CardRegistration(set = "THB", collectorNumber = "316")
public class TreacherousBlessing extends Card {

    public TreacherousBlessing() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(3));
        addTrigger(Triggers.youCastSpell().anySpell(), new LoseLifeEffect(1));
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new SacrificeSelfEffect());
    }
}
