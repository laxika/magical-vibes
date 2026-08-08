package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BOK", collectorNumber = "89")
public class ToshiroUmezawa extends Card {

    public ToshiroUmezawa() {
        // Whenever a creature an opponent controls dies, you may cast target instant card from your
        // graveyard. If that spell would be put into a graveyard, exile it instead.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new GrantTargetGraveyardCardCastEffect(
                new CardTypePredicate(CardType.INSTANT),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                true));
    }
}
