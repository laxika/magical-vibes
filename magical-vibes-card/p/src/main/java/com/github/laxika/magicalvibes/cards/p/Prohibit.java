package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;

@CardRegistration(set = "INV", collectorNumber = "67")
@CardRegistration(set = "DDM", collectorNumber = "25")
@CardRegistration(set = "MH1", collectorNumber = "64")
public class Prohibit extends Card {

    public Prohibit() {
        addStaticEffect(new KickerEffect("{2}"));
        // Any spell may be targeted; the mana value is checked as the spell resolves.
        addSpellEffect(new ConditionalReplacementEffect(
                new Kicked(),
                new ConditionalEffect(new TargetSpellMatches(new StackEntryMaxManaValuePredicate(2)),
                        new CounterSpellEffect()),
                new ConditionalEffect(new TargetSpellMatches(new StackEntryMaxManaValuePredicate(4)),
                        new CounterSpellEffect())));
    }
}
