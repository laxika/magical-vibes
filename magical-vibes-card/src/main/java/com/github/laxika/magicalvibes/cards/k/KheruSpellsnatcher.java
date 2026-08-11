package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileThenGrantFreeCastPermissionEffect;

@CardRegistration(set = "KTK", collectorNumber = "45")
public class KheruSpellsnatcher extends Card {

    public KheruSpellsnatcher() {
        addMorph("{4}{U}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new CounterSpellAndExileThenGrantFreeCastPermissionEffect());
    }
}
