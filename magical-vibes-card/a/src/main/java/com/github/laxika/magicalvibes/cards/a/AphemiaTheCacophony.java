package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "84")
@CardRegistration(set = "THB", collectorNumber = "311")
public class AphemiaTheCacophony extends Card {

    public AphemiaTheCacophony() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ExileOwnGraveyardCardThenCreateTokenEffect(
                new CardTypePredicate(CardType.ENCHANTMENT),
                CreateTokenEffect.blackZombie(1)));
    }
}
