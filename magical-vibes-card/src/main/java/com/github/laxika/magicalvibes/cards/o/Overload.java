package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "157")
public class Overload extends Card {

    public Overload() {
        PermanentPredicate artifactWithManaValueTwoOrLess = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentMaxManaValuePredicate(2)));
        PermanentPredicate artifactWithManaValueFiveOrLess = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentMaxManaValuePredicate(5)));

        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));
        target(new PermanentPredicateTargetFilter(
                artifactWithManaValueTwoOrLess,
                "Target must be an artifact with mana value 2 or less.",
                artifactWithManaValueFiveOrLess
        )).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new DestroyTargetPermanentEffect(),
                new DestroyTargetPermanentEffect()
        ));
    }
}
