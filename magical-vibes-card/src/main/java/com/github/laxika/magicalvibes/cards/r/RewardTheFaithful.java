package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SCG", collectorNumber = "22")
public class RewardTheFaithful extends Card {

    public RewardTheFaithful() {
        target(0, 99).addEffect(EffectSlot.SPELL, new EachTargetPlayerGainsLifeEffect(
                new GreatestManaValueAmongControlled(new PermanentTruePredicate())));
    }
}
