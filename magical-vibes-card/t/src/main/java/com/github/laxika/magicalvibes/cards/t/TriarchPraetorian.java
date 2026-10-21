package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "40K", collectorNumber = "66")
public class TriarchPraetorian extends Card {

    public TriarchPraetorian() {
        addEffect(EffectSlot.ON_SELF_ENTERS_FROM_GRAVEYARD, SequenceEffect.of(
                new DrawCardEffect(2),
                new LoseLifeEffect(2)));

        addUnearth("{4}{B}");
    }
}
