package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "228")
@CardRegistration(set = "PZA", collectorNumber = "18")
public class SwordOfSinewAndSteel extends Card {

    public SwordOfSinewAndSteel() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED), GrantScope.EQUIPPED_CREATURE));

        setAllowSharedTargets(true);
        CardEffect destroyPlaneswalker = new DestroyTargetPermanentEffect();
        CardEffect destroyArtifact = new DestroyTargetPermanentEffect();
        addTrigger(Triggers.dealsCombatDamageToPlayer(), destroyPlaneswalker, destroyArtifact);
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(), "Target must be a planeswalker"), 0, 1)
                .bindEffect(destroyPlaneswalker);
        target(TargetFilters.artifact(), 0, 1).bindEffect(destroyArtifact);

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
