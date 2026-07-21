package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "4")
public class EtherswornShieldmage extends Card {

    public EtherswornShieldmage() {
        // Flash is auto-loaded from Scryfall.
        // "When this creature enters, prevent all damage that would be dealt to artifact creatures
        // this turn." Re-evaluated per damage event (ruling: not locked in at resolution), so it
        // protects any permanent that is an artifact creature at the moment damage would be dealt.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, PreventDamageEffect.allToMatchingPermanents(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()))));
    }
}
