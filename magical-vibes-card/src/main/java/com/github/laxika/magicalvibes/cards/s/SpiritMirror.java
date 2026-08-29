package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "48")
@CardRegistration(set = "TPR", collectorNumber = "35")
public class SpiritMirror extends Card {

    public SpiritMirror() {
        // At the beginning of your upkeep, if there are no Reflection tokens on the battlefield,
        // create a 2/2 white Reflection creature token.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AnyPlayerControlsPermanentCountAtMost(0, new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.REFLECTION),
                        new PermanentIsTokenPredicate()))),
                new CreateTokenEffect("Reflection", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.REFLECTION), Set.of(), Set.of())));

        // {0}: Destroy target Reflection.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new DestroyTargetPermanentEffect()),
                "{0}: Destroy target Reflection.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.REFLECTION),
                        "Target must be a Reflection")
        ));
    }
}
