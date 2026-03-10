package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "33")
public class SteelSabotage extends Card {

    public SteelSabotage() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target artifact spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.ARTIFACT_SPELL)),
                                "Target must be an artifact spell."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target artifact to its owner's hand",
                        new ReturnTargetPermanentToHandEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsArtifactPredicate(),
                                "Target must be an artifact."
                        )
                )
        )));
    }
}
