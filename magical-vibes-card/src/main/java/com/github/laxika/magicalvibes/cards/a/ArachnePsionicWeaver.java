package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "2")
@CardRegistration(set = "SPM", collectorNumber = "245")
@CardRegistration(set = "OM1", collectorNumber = "23")
public class ArachnePsionicWeaver extends Card {

    public ArachnePsionicWeaver() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}"),
                new ReturnPermanentsCost(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsTappedPredicate()))))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardTypeOnEnterEffect(List.of(CardType.CREATURE), true));
        addEffect(EffectSlot.STATIC,
                new IncreaseSpellCostEffect(new CardHasSourceChosenCardTypePredicate(), 1,
                        CostModificationScope.ALL));
    }
}
