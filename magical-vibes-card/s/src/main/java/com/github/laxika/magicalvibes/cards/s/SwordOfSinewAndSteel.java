package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "228")
@CardRegistration(set = "PZA", collectorNumber = "18")
public class SwordOfSinewAndSteel extends Card {

    public SwordOfSinewAndSteel() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED), GrantScope.EQUIPPED_CREATURE));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(), "Target must be a planeswalker"), 0, 1)
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DestroyTargetPermanentEffect());
        target(TargetFilters.artifact(), 0, 1)
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DestroyTargetPermanentEffect());

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
