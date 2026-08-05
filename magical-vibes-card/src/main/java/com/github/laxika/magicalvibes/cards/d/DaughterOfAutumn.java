package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "85")
public class DaughterOfAutumn extends Card {

    public DaughterOfAutumn() {
        // {W}: The next 1 damage that would be dealt to target white creature this turn is dealt to
        // Daughter of Autumn instead.
        PermanentPredicate whiteCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new RedirectNextDamageEffect(RedirectRole.TARGET, RedirectRole.SOURCE_PERMANENT,
                        new Fixed(1), TargetCategory.CREATURE, whiteCreature)),
                "{W}: The next 1 damage that would be dealt to target white creature this turn is dealt to Daughter of Autumn instead.",
                new PermanentPredicateTargetFilter(whiteCreature, "Target must be a white creature")));
    }
}
