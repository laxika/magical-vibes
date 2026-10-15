package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "26")
@CardRegistration(set = "OTC", collectorNumber = "62")
public class DeadBeforeSunrise extends Card {

    private static final Set<CardSubtype> OUTLAW_SUBTYPES = Set.of(
            CardSubtype.ASSASSIN,
            CardSubtype.MERCENARY,
            CardSubtype.PIRATE,
            CardSubtype.ROGUE,
            CardSubtype.WARLOCK);

    public DeadBeforeSunrise() {
        PermanentHasAnySubtypePredicate outlaws = new PermanentHasAnySubtypePredicate(OUTLAW_SUBTYPES);

        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 0, outlaws));
        addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new DealDamageToAnyTargetEffect(new SourcePower())),
                        "{T}: This creature deals damage equal to its power to target creature.",
                        TargetFilters.creature()),
                GrantScope.OWN_CREATURES,
                outlaws,
                EffectDuration.UNTIL_END_OF_TURN));
    }
}
