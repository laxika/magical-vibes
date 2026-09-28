package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasManaAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "248")
public class RaggadraggaGoregutsBoss extends Card {

    public RaggadraggaGoregutsBoss() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                2,
                2,
                GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasManaAbilityPredicate()))));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasManaAbilityPredicate(),
                        new UntapTriggeringPermanentEffect()));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new BoostTargetCreatureEffect(7, 7),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                null,
                TargetFilters.creature(),
                null,
                false,
                false,
                new SpellManaSpentAtLeast(7),
                0,
                0,
                false,
                false,
                0,
                false));
    }
}
