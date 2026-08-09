package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "162")
public class ThatWhichWasTaken extends Card {

    public ThatWhichWasTaken() {
        PermanentHasCountersPredicate hasDivinityCounter = new PermanentHasCountersPredicate(CounterType.DIVINITY);

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE, GrantScope.SELF, hasDivinityCounter));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE, GrantScope.ALL_PERMANENTS, hasDivinityCounter));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.DIVINITY)),
                "{4}, {T}: Put a divinity counter on target permanent other than That Which Was Taken.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "Target must be another permanent")));
    }
}
