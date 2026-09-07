package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "21")
@CardRegistration(set = "ALL", collectorNumber = "3")
public class Exile extends Card {

    public Exile() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.WHITE))))),
                "Target must be a nonwhite attacking creature"
        ))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentThenEffect(
                        EventStat.TOUGHNESS,
                        new GainLifeEffect(new EventValue()),
                        ThenEffectRecipient.CONTROLLER));
    }
}
