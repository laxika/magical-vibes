package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithTriggeringSpellPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "6")
@CardRegistration(set = "LTC", collectorNumber = "89")
@CardRegistration(set = "LTC", collectorNumber = "463")
public class GandalfWestwardVoyager extends Card {

    public GandalfWestwardVoyager() {
        HappenedThisWay sharesACardType = HappenedThisWay.toACard(StepKind.REVEAL,
                new CardSharesCardTypeWithTriggeringSpellPredicate());
        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(5, true)),
                SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(Subjects.eachOpponent()),
                        new ConditionalEffect(sharesACardType, SequenceEffect.of(
                                new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens(),
                                new DrawCardEffect(1, Subjects.eachOpponent()))),
                        new ConditionalEffect(new NotCondition(sharesACardType), new DrawCardEffect(1))));
    }
}
