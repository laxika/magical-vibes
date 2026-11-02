package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "153")
public class LoafingGiant extends Card {

    public LoafingGiant() {
        var effect = new MillControllerThenIfMilledEffect(
                1,
                new CardTypePredicate(CardType.LAND),
                PreventDamageEffect.allCombatBySelf());
        addTrigger(Triggers.attacks(), effect);
        addTrigger(Triggers.blocks(), effect);
    }
}
