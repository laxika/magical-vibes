package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "112")
@CardRegistration(set = "VOW", collectorNumber = "114")
@CardRegistration(set = "DBL", collectorNumber = "381")
public class GluttonousGuest extends Card {

    public GluttonousGuest() {
        // When this creature enters, create a Blood token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofBloodToken(1));

        // Whenever you sacrifice a Blood token, you gain 1 life.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                new PermanentIsTokenPredicate())),
                        new GainLifeEffect(1)
                ));
    }
}
