package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DragonKamisEgg;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithHatchingCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "NEO", collectorNumber = "181")
public class TheDragonKamiReborn extends Card {

    public TheDragonKamiReborn() {
        setBackFaceCard(new DragonKamisEgg());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new GainLifeEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new ExileOneFromTopCardsFaceDownWithHatchingCounterEffect(3));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new ExileOneFromTopCardsFaceDownWithHatchingCounterEffect(3));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "DragonKamisEgg";
    }
}
