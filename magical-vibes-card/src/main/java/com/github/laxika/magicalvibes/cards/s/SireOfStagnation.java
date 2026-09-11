package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "BFZ", collectorNumber = "206")
public class SireOfStagnation extends Card {

    public SireOfStagnation() {
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new ExileTopCardsOfTargetPlayerLibraryEffect(2), new DrawCardEffect(2)));
    }
}
