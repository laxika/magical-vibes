package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "INR", collectorNumber = "190")
public class CultivatorColossus extends Card {

    public CultivatorColossus() {
        // Cultivator Colossus's power and toughness are each equal to the number of lands you control.
        PermanentCount landsYouControl =
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl));

        // When this creature enters, you may put a land card from your hand onto the battlefield tapped.
        // If you do, draw a card and repeat this process.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                PutCardToBattlefieldEffect.tappedDrawAndRepeat(new CardTypePredicate(CardType.LAND), "land"));
    }
}
