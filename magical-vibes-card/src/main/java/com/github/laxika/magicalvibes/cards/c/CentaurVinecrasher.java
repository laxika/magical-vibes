package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "C15", collectorNumber = "35")
public class CentaurVinecrasher extends Card {

    public CentaurVinecrasher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.ANY_PLAYER)));

        addEffect(EffectSlot.GRAVEYARD_ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new MayPayManaEffect(
                        "{G}{G}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {G}{G} to return Centaur Vinecrasher from your graveyard to your hand?"));
    }
}
