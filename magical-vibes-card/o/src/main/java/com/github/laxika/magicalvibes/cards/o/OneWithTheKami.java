package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEC", collectorNumber = "27")
@CardRegistration(set = "NEC", collectorNumber = "69")
public class OneWithTheKami extends Card {

    public OneWithTheKami() {
        target(TargetFilters.creatureYouControl());

        var spiritToken = new CreateTokenEffect(new EventValue(), "Spirit", 1, 1, null,
                List.of(CardSubtype.SPIRIT), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, spiritToken);

        var anotherModifiedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsModifiedPredicate(),
                new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate())));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(anotherModifiedCreature, spiritToken));
    }
}
