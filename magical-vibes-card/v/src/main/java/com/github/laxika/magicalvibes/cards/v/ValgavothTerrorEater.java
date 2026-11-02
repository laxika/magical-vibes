package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "120")
public class ValgavothTerrorEater extends Card {

    public ValgavothTerrorEater() {
        PermanentAllOfPredicate nonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessSacrificesEffect(nonland, "nonland permanent", 3));
        addStaticEffect(ExileOpponentCardsInsteadOfGraveyardEffect.forCardsYouDidntControl(true));
        addStaticEffect(AllowCastFromCardsExiledWithSourceEffect
                .payingLifeEqualToManaValue());
    }
}
