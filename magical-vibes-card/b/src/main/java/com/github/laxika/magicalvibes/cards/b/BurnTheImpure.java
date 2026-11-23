package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MBS", collectorNumber = "59")
public class BurnTheImpure extends Card {

    public BurnTheImpure() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(3))
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentHasKeywordPredicate(Keyword.INFECT)),
                        new DealDamageToPlayersEffect(3, Subjects.controllerOf(Subjects.target()))));
    }
}
