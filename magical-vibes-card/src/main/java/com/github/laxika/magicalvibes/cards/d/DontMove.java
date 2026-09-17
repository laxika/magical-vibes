package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "43")
public class DontMove extends Card {

    public DontMove() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()))));

        var destroyTappedCreature = new TriggeringPermanentConditionalEffect(
                new PermanentIsCreaturePredicate(),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
        addEffect(EffectSlot.SPELL, new RegisterGlobalTriggeredAbilityUntilNextTurnEffect(
                EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, destroyTappedCreature));
        addEffect(EffectSlot.SPELL, new RegisterGlobalTriggeredAbilityUntilNextTurnEffect(
                EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, destroyTappedCreature));
    }
}
