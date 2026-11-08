package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.KioraSovereignOfTheDeepRevealAndCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "35")
@CardRegistration(set = "MAT", collectorNumber = "85")
@CardRegistration(set = "MAT", collectorNumber = "135")
@CardRegistration(set = "MAT", collectorNumber = "170")
@CardRegistration(set = "MAT", collectorNumber = "214")
public class KioraSovereignOfTheDeep extends Card {

    public KioraSovereignOfTheDeep() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.KRAKEN),
                        new CardSubtypePredicate(CardSubtype.LEVIATHAN),
                        new CardSubtypePredicate(CardSubtype.OCTOPUS),
                        new CardSubtypePredicate(CardSubtype.SERPENT)))).castFrom(Zone.HAND),
                new KioraSovereignOfTheDeepRevealAndCastEffect());
    }
}
