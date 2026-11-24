package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNonlandPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;

@CardRegistration(set = "SNC", collectorNumber = "219")
public class SchemingFence extends Card {

    public SchemingFence() {
        addStaticEffect(new ChooseNonlandPermanentOnEnterEffect());
        addStaticEffect(new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(
                new PermanentIsChosenPermanentPredicate()));
        addStaticEffect(GainActivatedAbilitiesOfOwnCreaturesEffect.of(Subjects.allPermanents(),
                new PermanentIsChosenPermanentPredicate()).exceptLoyaltyAbilities());
        addStaticEffect(SpendManaAsAnyColorForActivatedAbilitiesEffect.forBorrowedAbilities());
    }
}
