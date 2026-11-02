package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "114")
public class RakshasaGravecaller extends Card {

    public RakshasaGravecaller() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));
        addTrigger(Triggers.exploits(), CreateTokenEffect.blackZombie(2));
    }
}
