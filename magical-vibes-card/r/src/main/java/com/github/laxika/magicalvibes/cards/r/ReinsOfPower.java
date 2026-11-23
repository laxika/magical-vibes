package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "STH", collectorNumber = "41")
@CardRegistration(set = "CMD", collectorNumber = "57")
@CardRegistration(set = "C15", collectorNumber = "103")
@CardRegistration(set = "C16", collectorNumber = "96")
public class ReinsOfPower extends Card {

    public ReinsOfPower() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addSpellEffect(new UntapPermanentsEffect(
                        Subjects.controlledBy(Subjects.you(), new PermanentIsCreaturePredicate())))
                .addSpellEffect(new UntapPermanentsEffect(
                        Subjects.controlledBy(Subjects.targetPlayerDeclaredElsewhere(),
                                new PermanentIsCreaturePredicate())))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.otherCreaturesYouControl()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.creaturesTargetPlayerControls()))
                .addSpellEffect(new ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect(
                        new PermanentIsCreaturePredicate(), Duration.UNTIL_END_OF_TURN));
    }
}
