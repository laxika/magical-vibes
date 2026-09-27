package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForEachDestroyedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1789")
public class BloodMoney extends Card {

    public BloodMoney() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(),
                new CreateTokenForEachDestroyedPermanentControllerEffect(
                        CreateTokenEffect.ofTappedTreasureToken(1), true)));
    }
}
