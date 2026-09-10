package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "120")
public class ValgavothTerrorEater extends Card {

    public ValgavothTerrorEater() {
        PermanentAllOfPredicate nonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessSacrificesEffect(nonland, "nonland permanent", 3));
        addEffect(EffectSlot.STATIC, new ExileOpponentCardsInsteadOfGraveyardEffect(true));
        addEffect(EffectSlot.STATIC, AllowCastFromCardsExiledWithSourceEffect
                .payingLifeEqualToManaValue());
    }
}
