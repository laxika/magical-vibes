package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsDownToFewestEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToFewestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "4ED", collectorNumber = "6")
public class Balance extends Card {

    public Balance() {
        // Each player keeps a number of lands / hand cards / creatures equal to the fewest any
        // player has, then sacrifices or discards the rest; each step is recomputed independently.
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesDownToFewestEffect(new PermanentIsLandPredicate()));
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsDownToFewestEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesDownToFewestEffect(new PermanentIsCreaturePredicate()));
    }
}
