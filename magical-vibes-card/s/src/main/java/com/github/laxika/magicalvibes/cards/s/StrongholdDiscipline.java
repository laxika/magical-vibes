package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "10E", collectorNumber = "181")
@CardRegistration(set = "NEM", collectorNumber = "73")
public class StrongholdDiscipline extends Card {

    public StrongholdDiscipline() {
        addSpellEffect(new LoseLifeEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                Subjects.eachPlayer()));
    }
}
