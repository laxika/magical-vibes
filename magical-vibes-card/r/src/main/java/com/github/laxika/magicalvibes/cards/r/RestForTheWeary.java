package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WWK", collectorNumber = "18")
public class RestForTheWeary extends Card {

    public RestForTheWeary() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.LAND), 1),
                new TargetPlayerGainsLifeEffect(4),
                new TargetPlayerGainsLifeEffect(8)
        ));
    }
}
