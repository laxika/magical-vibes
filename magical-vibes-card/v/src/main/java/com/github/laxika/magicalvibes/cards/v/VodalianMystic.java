package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "33")
public class VodalianMystic extends Card {

    public VodalianMystic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(SetTargetColorEffect.chosenColorForSpell()),
                "{T}: Target instant or sorcery spell becomes the color of your choice.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        "Target must be an instant or sorcery spell.")));
    }
}
