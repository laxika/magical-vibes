package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "THB", collectorNumber = "84")
public class AphemiaTheCacophony extends Card {

    public AphemiaTheCacophony() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ExileOwnGraveyardCardThenEffect(
                new CardTypePredicate(CardType.ENCHANTMENT),
                CreateTokenEffect.blackZombie(1)));
    }
}
