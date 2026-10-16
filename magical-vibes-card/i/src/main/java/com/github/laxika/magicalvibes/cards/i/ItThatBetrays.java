package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSacrificedCardUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "ROE", collectorNumber = "7")
@CardRegistration(set = "DDP", collectorNumber = "43")
@CardRegistration(set = "SLD", collectorNumber = "1176")
public class ItThatBetrays extends Card {

    public ItThatBetrays() {
        // Annihilator 2.
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                2, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));
        addEffect(EffectSlot.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED,
                new ReturnSacrificedCardUnderControlEffect());
    }
}
