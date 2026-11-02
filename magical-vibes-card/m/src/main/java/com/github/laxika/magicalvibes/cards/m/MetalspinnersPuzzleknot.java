package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "221")
@CardRegistration(set = "2XM", collectorNumber = "273")
public class MetalspinnersPuzzleknot extends Card {

    public MetalspinnersPuzzleknot() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1), new LoseLifeEffect(1)),
                "{2}{B}, Sacrifice this artifact: You draw a card and you lose 1 life."
        ));
    }
}
