package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "153")
public class KurkeshOnakkeAncient extends Card {

    public KurkeshOnakkeAncient() {
        // Whenever you activate an ability of an artifact, if it isn't a mana ability, you may pay
        // {R}. If you do, copy that ability. You may choose new targets for the copy.
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect("{R}",
                        new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT))));
    }
}
