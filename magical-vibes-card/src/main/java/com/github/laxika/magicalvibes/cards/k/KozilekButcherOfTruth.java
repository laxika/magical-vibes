package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "ROE", collectorNumber = "6")
public class KozilekButcherOfTruth extends Card {

    public KozilekButcherOfTruth() {
        addEffect(EffectSlot.ON_SELF_CAST, new DrawCardEffect(4));

        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                4, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
