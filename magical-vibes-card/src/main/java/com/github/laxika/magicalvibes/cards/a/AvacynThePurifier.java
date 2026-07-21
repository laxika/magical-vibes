package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

public class AvacynThePurifier extends Card {

    public AvacynThePurifier() {
        // When this creature transforms into Avacyn, the Purifier, it deals 3 damage to each other
        // creature and each opponent.
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE, new MassDamageEffect(3, false, false,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT));
    }
}
