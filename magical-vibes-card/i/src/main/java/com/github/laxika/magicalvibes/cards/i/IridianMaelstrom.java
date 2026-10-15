package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "12")
@CardRegistration(set = "DMC", collectorNumber = "88")
public class IridianMaelstrom extends Card {

    public IridianMaelstrom() {
        var allColors = new PermanentAllOfPredicate(List.of(
                new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                new PermanentColorInPredicate(Set.of(CardColor.RED)),
                new PermanentColorInPredicate(Set.of(CardColor.GREEN))));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(allColors)))));
    }
}
