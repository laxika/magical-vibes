package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YSOS", collectorNumber = "10")
public class AlgorithmicFerocity extends Card {

    public AlgorithmicFerocity() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.target()))
                .addSpellEffect(BoostEffect.target(
                        new MatchingCardsInHand(CountScope.CONTROLLER, new CardMinManaValuePredicate(4)), new Fixed(0)));

        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new FightTargetsEffect());
    }
}
