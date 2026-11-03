package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "148")
@CardRegistration(set = "OM1", collectorNumber = "144")
public class SpiderManifestation extends Card {

    public SpiderManifestation() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(3))),
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
