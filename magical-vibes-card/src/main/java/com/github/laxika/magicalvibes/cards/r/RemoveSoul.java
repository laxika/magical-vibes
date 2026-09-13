package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "117")
@CardRegistration(set = "10E", collectorNumber = "100")
@CardRegistration(set = "9ED", collectorNumber = "93")
@CardRegistration(set = "7ED", collectorNumber = "95")
@CardRegistration(set = "8ED", collectorNumber = "95")
@CardRegistration(set = "6ED", collectorNumber = "94")
@CardRegistration(set = "CHR", collectorNumber = "25")
@CardRegistration(set = "S99", collectorNumber = "49")
@CardRegistration(set = "LEG", collectorNumber = "72")
@CardRegistration(set = "ME3", collectorNumber = "47")
public class RemoveSoul extends Card {

    public RemoveSoul() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryCardTypeInPredicate(Set.of(CardType.CREATURE)),
                "Target must be a creature spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
