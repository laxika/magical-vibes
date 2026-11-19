package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerCantAttackTargetPlayerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantSacrificeTargetThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "113")
@CardRegistration(set = "LTC", collectorNumber = "432")
public class CallForAid extends Card {

    public CallForAid() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));

        addSpellEffect(new GainControlOfAllPermanentsTargetPlayerControlsEffect(
                new PermanentIsCreaturePredicate(),
                Duration.UNTIL_END_OF_TURN,
                List.of(
                        new UntapPermanentsEffect(Subjects.target()),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        new ControllerCantSacrificeTargetThisTurnEffect())));
        addSpellEffect(new ControllerCantAttackTargetPlayerThisTurnEffect());
    }
}
