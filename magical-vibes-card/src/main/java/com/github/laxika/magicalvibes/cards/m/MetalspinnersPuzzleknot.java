package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "221")
public class MetalspinnersPuzzleknot extends Card {

    public MetalspinnersPuzzleknot() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1), new LoseLifeEffect(1)),
                "{2}{B}, Sacrifice this artifact: You draw a card and you lose 1 life."
        ));
    }
}
