package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;

@CardRegistration(set = "YONE", collectorNumber = "7")
public class TezzeretsReckoning extends Card {

    public TezzeretsReckoning() {
        addEffect(EffectSlot.SPELL, new SeekLibraryEffect(
                new Fixed(3), null, LibrarySearchDestination.EXILE_WITH_SOURCE, null, true));
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, false, 0, null, false, false, false,
                ExileAccessScope.CONTROLLER, false, false, true, false, false, null,
                true, false, false, false, false));
    }
}
