package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfOpponentAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DOM", collectorNumber = "204")
public class ShannaSisaysLegacy extends Card {

    public ShannaSisaysLegacy() {
        // Shanna can't be the target of abilities your opponents control.
        addEffect(EffectSlot.STATIC, new CantBeTargetOfOpponentAbilitiesEffect());

        // Shanna gets +1/+1 for each creature you control.
        addEffect(EffectSlot.STATIC, new BoostSelfPerControlledPermanentEffect(1, 1,
                new PermanentIsCreaturePredicate()));
    }
}
