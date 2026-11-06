package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasManaAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "248")
public class RaggadraggaGoregutsBoss extends Card {

    public RaggadraggaGoregutsBoss() {
        addStaticEffect(new StaticBoostEffect(
                2,
                2,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasManaAbilityPredicate()))));

        addTrigger(Triggers.creatureYouControlAttacks().withFilter(new PermanentHasManaAbilityPredicate()),
                new UntapPermanentsEffect(TapUntapScope.TRIGGERING));

        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell().anySpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(7)),
                new UntapPermanentsEffect(TapUntapScope.TARGET),
                new BoostTargetCreatureEffect(7, 7),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
