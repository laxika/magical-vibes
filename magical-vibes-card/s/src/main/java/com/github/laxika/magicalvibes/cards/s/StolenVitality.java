package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "161")
@CardRegistration(set = "DBL", collectorNumber = "161")
public class StolenVitality extends Card {

    public StolenVitality() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 1))
                .addSpellEffect(new ConditionalEffect(new ControllerTurn(),
                        new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target())))
                .addSpellEffect(new ConditionalEffect(new NotControllerTurn(),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target())));
    }
}
