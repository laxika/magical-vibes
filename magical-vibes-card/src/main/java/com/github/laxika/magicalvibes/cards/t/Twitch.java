package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "121")
public class Twitch extends Card {

    public Twitch() {

        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new TapOrUntapTargetPermanentEffect(Set.of(CardType.ARTIFACT, CardType.CREATURE, CardType.LAND)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
