package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "72")
public class PhantasmalImage extends Card {

    public PhantasmalImage() {
        // You may have this creature enter as a copy of any creature on the battlefield, except it's
        // an Illusion in addition to its other types and it has "When this creature becomes the
        // target of a spell or ability, sacrifice it."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature",
                Set.of(CardSubtype.ILLUSION),
                Map.of(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                        List.<CardEffect>of(new SacrificeSelfEffect()))
        ));
    }
}
