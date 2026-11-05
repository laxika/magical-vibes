package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TheMasterGallifreysEndEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "WHO", collectorNumber = "144")
@CardRegistration(set = "WHO", collectorNumber = "427")
@CardRegistration(set = "WHO", collectorNumber = "543")
@CardRegistration(set = "WHO", collectorNumber = "749")
@CardRegistration(set = "WHO", collectorNumber = "1018")
@CardRegistration(set = "WHO", collectorNumber = "1134")
public class TheMasterGallifreysEnd extends Card {

    public TheMasterGallifreysEnd() {
        addTrigger(Triggers.nontokenCreatureDies(new PermanentIsArtifactPredicate(), ObjectRelation.YOU_CONTROL),
                new MayEffect(new TheMasterGallifreysEndEffect(),
                        "Exile that creature and make an opponent face a villainous choice?"));
    }
}
