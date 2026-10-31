package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "BFZ", collectorNumber = "87")
@CardRegistration(set = "M3C", collectorNumber = "196")
public class UginsInsight extends Card {

    public UginsInsight() {
        addSpellEffect(new ScryEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate())));
        addSpellEffect(new DrawCardEffect(3));
    }
}
