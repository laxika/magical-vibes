package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DragonKamisEgg;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithHatchingCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "181")
public class TheDragonKamiReborn extends Card {

    public TheDragonKamiReborn() {
        setBackFaceCard(new DragonKamisEgg());

        addTrigger(Triggers.chapter(1), new GainLifeEffect(2));
        addTrigger(Triggers.chapter(1),
                new ExileOneFromTopCardsFaceDownWithHatchingCounterEffect(3));
        addTrigger(Triggers.chapter(2), new GainLifeEffect(2));
        addTrigger(Triggers.chapter(2),
                new ExileOneFromTopCardsFaceDownWithHatchingCounterEffect(3));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "DragonKamisEgg";
    }
}
