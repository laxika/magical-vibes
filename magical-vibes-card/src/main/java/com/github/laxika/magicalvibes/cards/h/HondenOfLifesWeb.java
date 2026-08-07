package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "213")
public class HondenOfLifesWeb extends Card {

    public HondenOfLifesWeb() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER),
                "Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));
    }
}
