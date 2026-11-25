package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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

        addSpellEffect(new BoostAllOwnCreaturesEffect(1, 0, outlaws));
        addSpellEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(DealDamageEffect.toAnyTarget(new SourcePower())),
                        "{T}: This creature deals damage equal to its power to target creature.",
                        TargetFilters.creature()),
                Subjects.otherCreaturesYouControl(),
                outlaws,
                Duration.UNTIL_END_OF_TURN));
    }
}
