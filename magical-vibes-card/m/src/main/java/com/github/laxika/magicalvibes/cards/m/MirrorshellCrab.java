package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "63")
public class MirrorshellCrab extends Card {

    public MirrorshellCrab() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), UnlessPaysEffect.counterUnlessPays(3));

        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(UnlessPaysEffect.counterUnlessPays(3)),
                "Channel — {2}{U}, Discard this card: Counter target spell or ability unless its controller pays {3}.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryHasTargetPredicate(),
                        "Target must be a spell or ability on the stack."
                )
        ));
    }
}
