package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "130")
public class DraconicDestiny extends Card {

    public DraconicDestiny() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(
                        1, 1, Set.of(Keyword.FLYING, Keyword.HASTE), GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.DRAGON, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{1}",
                                List.of(new BoostSelfEffect(1, 0)),
                                "{1}: This creature gets +1/+0 until end of turn."
                        ),
                        GrantScope.ENCHANTED_CREATURE
                ))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
