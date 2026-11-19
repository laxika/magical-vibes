package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SCG", collectorNumber = "56")
public class CabalConditioning extends Card {

    public CabalConditioning() {
        target(0, 99).addSpellEffect(new DiscardEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate()),
                Subjects.targetPlayer()));
    }
}
