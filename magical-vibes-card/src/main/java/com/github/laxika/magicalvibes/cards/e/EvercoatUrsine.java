package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "BLC", collectorNumber = "30")
@CardRegistration(set = "BLC", collectorNumber = "64")
public class EvercoatUrsine extends Card {

    public EvercoatUrsine() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ExileOneFromTopCardsFaceDownWithSourceEffect(3, true),
                new ExileOneFromTopCardsFaceDownWithSourceEffect(3, true)));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ConditionalEffect(new SourceExiledCardsThreshold(1),
                        new MayPlayCardExiledWithSourceEffect()));
    }
}
