package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "120")
@CardRegistration(set = "DDJ", collectorNumber = "65")
public class Doomgape extends Card {

    public Doomgape() {
        // At the beginning of your upkeep, sacrifice a creature. You gain life equal to that
        // creature's toughness. (Trample is auto-loaded from Scryfall.)
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                new GainLifeEffect(AmountThisWay.sacrificedToughness())));
    }
}
