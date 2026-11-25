package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH1", collectorNumber = "141")
public class Pyrophobia extends Card {

    public Pyrophobia() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(3))
                .addSpellEffect(new CantBlockThisTurnEffect(
                        Subjects.controlledBy(Subjects.eachPlayer(), new PermanentHasSubtypePredicate(CardSubtype.COWARD))));
    }
}
