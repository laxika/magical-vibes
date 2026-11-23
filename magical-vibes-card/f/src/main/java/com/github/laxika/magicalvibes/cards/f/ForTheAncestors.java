package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasTypeChosenThisResolutionPredicate;

@CardRegistration(set = "CMM", collectorNumber = "740")
@CardRegistration(set = "CMM", collectorNumber = "770")
public class ForTheAncestors extends Card {

    public ForTheAncestors() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(LookAtTopCardsEffect.mayRevealUpToToHandRestOnBottomRandom(
                6, new CardHasTypeChosenThisResolutionPredicate(), Integer.MAX_VALUE));
        addCastingOption(new FlashbackCast("{3}{G}"));
    }
}
