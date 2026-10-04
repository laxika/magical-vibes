package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentDamagedPlayerControlsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "NPH", collectorNumber = "28")
public class ArmWithAether extends Card {

    public ArmWithAether() {
        addEffect(EffectSlot.SPELL, new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                EffectSlot.ON_DAMAGE_TO_OPPONENT,
                new MayEffect(new ReturnPermanentDamagedPlayerControlsToHandEffect(
                        new PermanentIsCreaturePredicate()), "Return target creature to its owner's hand?")));
    }
}
