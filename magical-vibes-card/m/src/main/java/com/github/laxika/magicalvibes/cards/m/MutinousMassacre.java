package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesOfChosenParityEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "EOE", collectorNumber = "222")
public class MutinousMassacre extends Card {

    public MutinousMassacre() {
        addSpellEffect(new ChooseManaValueParityAtResolutionEffect());
        addSpellEffect(new DestroyAllCreaturesOfChosenParityEffect());
        addSpellEffect(new GainControlOfAllPermanentsMatchingEffect(
                new PermanentIsCreaturePredicate(), ControlDuration.END_OF_TURN));
        addSpellEffect(new UntapPermanentsEffect(
                TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));
        addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
    }
}
