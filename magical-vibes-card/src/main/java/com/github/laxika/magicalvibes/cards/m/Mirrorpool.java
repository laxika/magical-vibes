package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "174")
public class Mirrorpool extends Card {

    public Mirrorpool() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{C}",
                List.of(new SacrificeSelfCost(), new CopySpellEffect()),
                "{2}{C}, {T}, Sacrifice this land: Copy target instant or sorcery spell you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.SORCERY_SPELL)),
                                new StackEntryControlledByPredicate())),
                        "Target must be an instant or sorcery spell you control.")));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{C}",
                List.of(new SacrificeSelfCost(), new CreateTokenCopyOfTargetPermanentEffect()),
                "{4}{C}, {T}, Sacrifice this land: Create a token that's a copy of target creature you control.",
                TargetFilters.creatureYouControl()));
    }
}
