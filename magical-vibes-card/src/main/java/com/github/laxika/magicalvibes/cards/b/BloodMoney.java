package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyedPermanentCountScope;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1789")
@CardRegistration(set = "LCC", collectorNumber = "183")
@CardRegistration(set = "HBG", collectorNumber = "146")
public class BloodMoney extends Card {

    public BloodMoney() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(),
                CreateTokenEffect.ofTappedTreasureToken(new EventValue()),
                DestroyedPermanentCountScope.ALL_NONTOKEN));
    }
}
