package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "12")
public class UlamogTheInfiniteGyre extends Card {

    public UlamogTheInfiniteGyre() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_SELF_CAST,
                new DestroyTargetPermanentEffect());

        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                4, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
