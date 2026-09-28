package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSourceAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "44")
@CardRegistration(set = "LCC", collectorNumber = "76")
public class MistDancer extends Card {

    public MistDancer() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, Set.of(Keyword.FLYING), GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopiesOfSourceAttackingOpponentsEffect()
                ),
                "Encore {5}{U}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
