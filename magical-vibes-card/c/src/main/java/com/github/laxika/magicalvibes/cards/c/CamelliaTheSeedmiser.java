package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ForageOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "207")
public class CamelliaTheSeedmiser extends Card {

    public CamelliaTheSeedmiser() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.MENACE,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL)));

        addTrigger(new TriggerCondition(Triggers.sacrificed(
                                new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                                ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE,
                new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()));

        PermanentPredicate otherSquirrels = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(ForageOrPayManaCost.forageOnly(), new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, otherSquirrels)),
                "{2}, Forage: Put a +1/+1 counter on each other Squirrel you control."
        ));
    }
}
