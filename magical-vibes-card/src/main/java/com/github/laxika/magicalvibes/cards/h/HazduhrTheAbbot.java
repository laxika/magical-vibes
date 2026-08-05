package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "8")
public class HazduhrTheAbbot extends Card {

    public HazduhrTheAbbot() {
        // {X}, {T}: The next X damage that would be dealt this turn to target white creature you
        // control is dealt to Hazduhr the Abbot instead.
        // The "you control" half rides on the ControlledPermanentPredicateTargetFilter: the effect's
        // targetSpec is evaluated without a source permanent, so a controller predicate cannot work there.
        PermanentPredicate whiteCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new RedirectNextDamageEffect(RedirectRole.TARGET, RedirectRole.SOURCE_PERMANENT,
                        new XValue(), TargetCategory.CREATURE, whiteCreature)),
                "{X}, {T}: The next X damage that would be dealt this turn to target white creature you control is dealt to Hazduhr the Abbot instead.",
                new ControlledPermanentPredicateTargetFilter(whiteCreature,
                        "Target must be a white creature you control")));
    }
}
