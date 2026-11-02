package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "112")
@CardRegistration(set = "VOW", collectorNumber = "114")
@CardRegistration(set = "DBL", collectorNumber = "381")
public class GluttonousGuest extends Card {

    public GluttonousGuest() {
        // When this creature enters, create a Blood token.
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofBloodToken(1));

        // Whenever you sacrifice a Blood token, you gain 1 life.
        addTrigger(Triggers.sacrificed(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                new PermanentIsTokenPredicate())),
                        ObjectRelation.YOU_CONTROL),
                new GainLifeEffect(1));
    }
}
