package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DRC", collectorNumber = "4")
public class TemmetNaktamunsWill extends Card {

    public TemmetNaktamunsWill() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                SequenceEffect.of(
                        new DrawCardEffect(),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)));

        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new BoostAllOwnCreaturesEffect(1, 1,
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));
    }
}
