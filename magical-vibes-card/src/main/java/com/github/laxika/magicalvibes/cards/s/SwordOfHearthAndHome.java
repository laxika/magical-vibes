package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "238")
public class SwordOfHearthAndHome extends Card {

    public SwordOfHearthAndHome() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(
                Set.of(CardColor.GREEN, CardColor.WHITE), GrantScope.EQUIPPED_CREATURE));

        target(new OwnedPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature you own"), 0, 1)
                .addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE,
                        SequenceEffect.of(
                                FlickerEffect.flickerTargetUnderYourControl(),
                                new SearchLibraryEffect(
                                        CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD)));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
