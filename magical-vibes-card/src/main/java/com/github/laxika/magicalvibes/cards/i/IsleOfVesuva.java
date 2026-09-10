package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OHOP", collectorNumber = "18")
public class IsleOfVesuva extends Card {

    public IsleOfVesuva() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(), Set.of(), null, null, Map.of(),
                                false, false, false, false,
                                false, true, null, Set.of())));
        target(TargetFilters.creature()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                new DestroyTargetPermanentAndAllWithSameNameEffect(new PermanentIsCreaturePredicate()));
    }
}
