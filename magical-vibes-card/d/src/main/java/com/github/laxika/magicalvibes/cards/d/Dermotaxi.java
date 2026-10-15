package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "224")
public class Dermotaxi extends Card {

    public Dermotaxi() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileCardFromGraveyardOnEnterEffect(
                        new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        new BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect(
                                Set.of(CardType.ARTIFACT), Set.of(CardSubtype.VEHICLE))),
                "Tap two untapped creatures you control: Until end of turn, this Vehicle becomes a copy "
                        + "of the exiled card, except it's a Vehicle artifact in addition to its other types."
        ));
    }
}
