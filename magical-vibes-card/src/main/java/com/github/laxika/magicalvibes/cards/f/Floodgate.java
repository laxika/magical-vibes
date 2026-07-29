package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "67")
public class Floodgate extends Card {

    public Floodgate() {
        // "When this creature has flying, sacrifice it." — state-triggered ability (rule 603.8);
        // the source predicate is layer-aware so granted flying (auras, pumps) counts.
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                List.of(new SacrificeSelfEffect()),
                "Floodgate's state-triggered ability"
        ));

        // "When this creature leaves the battlefield, it deals damage to each nonblue creature
        // without flying equal to half the number of Islands you control, rounded down."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new MassDamageEffect(
                new Divided(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND), CountScope.CONTROLLER), 2),
                false,
                false,
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLUE))),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))))
        ));
    }
}
