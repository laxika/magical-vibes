package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M20", collectorNumber = "93")
public class BonecladNecromancer extends Card {

    public BonecladNecromancer() {
        // When this creature enters, you may exile target creature card from a graveyard. If you
        // do, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExileGraveyardCardCreateTokenIfCreatureEffect(
                        new CardTypePredicate(CardType.CREATURE)),
                        "Exile target creature card from a graveyard?"));
    }
}
