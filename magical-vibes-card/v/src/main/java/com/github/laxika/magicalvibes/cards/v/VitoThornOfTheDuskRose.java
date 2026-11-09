package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "127")
@CardRegistration(set = "M21", collectorNumber = "362")
@CardRegistration(set = "SLD", collectorNumber = "2282")
@CardRegistration(set = "SLZ", collectorNumber = "52")
@CardRegistration(set = "SLZ", collectorNumber = "173")
@CardRegistration(set = "SLZ", collectorNumber = "294")
public class VitoThornOfTheDuskRose extends Card {

    public VitoThornOfTheDuskRose() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.youGainLife(),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{B}",
                List.of(
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.ALL_OWN_CREATURES)
                ),
                "{3}{B}{B}: Creatures you control gain lifelink until end of turn."
        ));
    }
}
