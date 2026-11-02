package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "115")
@CardRegistration(set = "DDK", collectorNumber = "3")
@CardRegistration(set = "MM2", collectorNumber = "101")
@CardRegistration(set = "A25", collectorNumber = "114")
public class VampireLacerator extends Card {

    public VampireLacerator() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), ConditionalEffect.unless(
                new NotCondition(new AnOpponentLifeAtMost(10)), new LoseLifeEffect(1)));
    }
}
