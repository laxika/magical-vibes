package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "27")
@CardRegistration(set = "MSC", collectorNumber = "326")
public class WinterSoldierRebornAvenger extends Card {

    public WinterSoldierRebornAvenger() {
        // Whenever Winter Soldier attacks, return target creature card with mana value less than
        // or equal to Winter Soldier's power from your graveyard to the battlefield. If a Hero
        // enters this way, it enters with an additional +1/+1 counter on it.
        addEffect(EffectSlot.ON_ATTACK, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .dynamicMaxManaValue(new SourcePower())
                .plusOneCountersIfSubtype(CardSubtype.HERO)
                .plusOneCounterCount(1)
                .build());
    }
}
