package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ONS", collectorNumber = "252")
public class ChainOfAcid extends Card {

    public ChainOfAcid() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                "Target must be a noncreature permanent"
        ))
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new MayEffect(
                        new CopyThisSpellForTargetControllerEffect(),
                        "Copy Chain of Acid?",
                        null,
                        MayChoicePlayer.TARGET_PERMANENT_CONTROLLER
                ));
    }
}
