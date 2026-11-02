package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "127")
public class GoblinChampion extends Card {

    public GoblinChampion() {
        addTrigger(Triggers.creatureYouControlAttacks(),
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));
    }
}
