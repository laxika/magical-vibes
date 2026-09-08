package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "52")
public class InsidiousWill extends Card {

    public InsidiousWill() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell",
                        new CounterSpellEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "You may choose new targets for target spell",
                        new MayEffect(new ChooseNewTargetsForTargetSpellEffect(),
                                "Choose new targets for the spell?")),
                new ChooseOneEffect.ChooseOneOption(
                        "Copy target instant or sorcery spell. You may choose new targets for the copy",
                        new CopySpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.SORCERY_SPELL)),
                                "Target must be an instant or sorcery spell."))
        )));
    }
}
