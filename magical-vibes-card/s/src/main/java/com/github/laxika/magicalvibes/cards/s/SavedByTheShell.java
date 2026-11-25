package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMT", collectorNumber = "132")
public class SavedByTheShell extends Card {

    public SavedByTheShell() {
        addStaticEffect(new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.TURTLE)),
                new ReduceOwnCastCostEffect(new Fixed(1))));

        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.target()));
    }
}
