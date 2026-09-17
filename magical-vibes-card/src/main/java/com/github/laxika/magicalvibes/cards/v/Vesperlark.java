package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "35")
public class Vesperlark extends Card {

    public Vesperlark() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{W}"))));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtMostPredicate(1))))
                        .targetGraveyard(true)
                        .build());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
