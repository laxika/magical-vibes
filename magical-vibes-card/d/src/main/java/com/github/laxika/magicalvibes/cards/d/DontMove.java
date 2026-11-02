package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "43")
@CardRegistration(set = "REX", collectorNumber = "1")
@CardRegistration(set = "REX", collectorNumber = "27")
public class DontMove extends Card {

    public DontMove() {
        addSpellEffect(new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()))));
        addSpellEffect(new RegisterGlobalTriggeredAbilityUntilNextTurnEffect(
                new TriggeredAbility(Triggers.creatureBecomesTapped(),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING))));
    }
}
