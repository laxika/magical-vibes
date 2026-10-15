package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "169")
@CardRegistration(set = "TMT", collectorNumber = "251")
public class SplinterRadicalRat extends Card {

    public SplinterRadicalRat() {
        PermanentAllOfPredicate ninjaCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.NINJA)));

        addEffect(EffectSlot.STATIC,
                new AdditionalTriggeredAbilityEffect(ninjaCreature, null, true, false));

        addActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{1}{U}: Target Ninja can't be blocked this turn.",
                new PermanentPredicateTargetFilter(ninjaCreature, "Target must be a Ninja creature")));
    }
}
