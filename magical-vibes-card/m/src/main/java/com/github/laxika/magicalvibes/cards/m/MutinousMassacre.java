package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "222")
public class MutinousMassacre extends Card {

    public MutinousMassacre() {
        addSpellEffect(new ChooseManaValueParityAtResolutionEffect());
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasChosenParityPredicate(PermanentHasChosenParityPredicate.Quality.MANA_VALUE)))));
        addSpellEffect(new GainControlOfAllPermanentsMatchingEffect(
                new PermanentIsCreaturePredicate(), Duration.UNTIL_END_OF_TURN));
        addSpellEffect(new UntapPermanentsEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentIsCreaturePredicate())));
        addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.otherCreaturesYouControl()));
    }
}
