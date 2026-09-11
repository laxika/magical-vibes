package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "79")
public class ScionOfDarkness extends Card {

    public ScionOfDarkness() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                        false, new CardTypePredicate(CardType.CREATURE), false));
        addCycling("{3}");
    }
}
