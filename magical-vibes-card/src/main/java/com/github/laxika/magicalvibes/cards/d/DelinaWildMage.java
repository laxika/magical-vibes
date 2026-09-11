package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "138")
public class DelinaWildMage extends Card {

    public DelinaWildMage() {
        CreateTokenCopyOfTargetPermanentEffect tokenCopy =
                CreateTokenCopyOfTargetPermanentEffect.tappedAttackingWithAttackTargetChoice(true, true);
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ATTACK,
                        RollD20Effect.withRepeatOnHighBranch(tokenCopy, tokenCopy, 14));
    }
}
