package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "60")
public class FieldOfReality extends Card {

    public FieldOfReality() {
        target(TargetFilters.creature())
                // Enchanted creature can't be blocked by Spirits.
                .addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(ReturnToHandEffect.self()),
                "{1}{U}: Return this Aura to its owner's hand."
        ));
    }
}
