package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "231")
public class SilverquillApprentice extends Card {

    public SilverquillApprentice() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> boost = List.of(new BoostTargetCreatureEffect(1, 0));

        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                boost.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell().withFilter(instantOrSorcery), boost.toArray(CardEffect[]::new));
    }
}
