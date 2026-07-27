package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "32")
public class ConsignToDream extends Card {

    public ConsignToDream() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect(
                new PermanentColorInPredicate(Set.of(CardColor.RED, CardColor.GREEN))));
    }
}
