package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "10")
public class AppaSteadfastGuardian extends Card {

    public AppaSteadfastGuardian() {
        PermanentPredicate anotherNonlandYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                anotherNonlandYouControl,
                "Target must be another nonland permanent you control"), 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AirbendTargetPermanentEffect());

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new CreateTokenEffect("Ally", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.ALLY), Set.of(), Set.of())),
                new StackEntryCastFromZonePredicate(Zone.EXILE)));
    }
}
