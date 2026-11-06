package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "KHM", collectorNumber = "73")
@CardRegistration(set = "KHM", collectorNumber = "347")
@CardRegistration(set = "KHM", collectorNumber = "400")
@CardRegistration(set = "TDC", collectorNumber = "164")
@CardRegistration(set = "LCC", collectorNumber = "168")
@CardRegistration(set = "WOC", collectorNumber = "106")
public class ReflectionsOfLittjara extends Card {

    public ReflectionsOfLittjara() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        // "Whenever you cast a spell of the chosen type, copy that spell." The Oracle text grants no
        // new-target choice, so the copy keeps its targets (CR 707.10c).
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenSubtypePredicate(false)),
                new CopyTriggeringSpellEffect(true).withoutNewTargetChoice());
    }
}
