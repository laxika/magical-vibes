package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "9a")
@CardRegistration(set = "ALL", collectorNumber = "9b")
public class KjeldoranPride extends Card {

    public KjeldoranPride() {
        // Enchant creature. Enchanted creature gets +1/+2.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.ENCHANTED_CREATURE));

        // {2}{U}: Attach this Aura to target creature other than enchanted creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new AttachSourceAuraToTargetCreatureEffect()),
                "{2}{U}: Attach this Aura to target creature other than enchanted creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate()))),
                        "Target must be a creature other than enchanted creature")
        ));
    }
}
