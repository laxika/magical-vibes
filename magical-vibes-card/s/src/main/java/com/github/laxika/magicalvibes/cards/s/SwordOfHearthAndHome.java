package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileTargetThenSearchLibraryPutBothOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "238")
@CardRegistration(set = "LTC", collectorNumber = "354")
@CardRegistration(set = "LTC", collectorNumber = "384")
public class SwordOfHearthAndHome extends Card {

    public SwordOfHearthAndHome() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new ProtectionFromColorsEffect(
                Set.of(CardColor.GREEN, CardColor.WHITE), GrantScope.EQUIPPED_CREATURE));

        target(new OwnedPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature you own"), 0, 1)
                .addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                        new ExileTargetThenSearchLibraryPutBothOntoBattlefieldEffect(
                                CardPredicateUtils.basicLand()));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
