package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "141")
public class PhyrexianSoulgorger extends Card {

    public PhyrexianSoulgorger() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                CumulativeUpkeepEffect.sacrifice(new PermanentIsCreaturePredicate()));
    }
}
