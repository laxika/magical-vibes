package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "7")
@CardRegistration(set = "BLC", collectorNumber = "43")
public class TheOddAcornGang extends Card {

    public TheOddAcornGang() {
        PermanentPredicate squirrel = new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL);
        PermanentPredicate squirrelCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), squirrel));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(
                                new BoostTargetCreatureEffect(2, 2),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                        ),
                        "{T}: Target Squirrel gets +2/+2 and gains trample until end of turn. "
                                + "Activate only as a sorcery.",
                        new PermanentPredicateTargetFilter(squirrelCreature, "Target must be a Squirrel"),
                        null,
                        null,
                        ActivationTimingRestriction.SORCERY_SPEED
                ),
                GrantScope.ALL_OWN_CREATURES,
                squirrel));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(squirrel, new DrawCardEffect(1), false, true));
    }
}
