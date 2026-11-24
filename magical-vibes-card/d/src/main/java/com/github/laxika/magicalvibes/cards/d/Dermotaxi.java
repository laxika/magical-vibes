package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "224")
public class Dermotaxi extends Card {

    public Dermotaxi() {
        addStaticEffect(new ExileCardFromGraveyardOnEnterEffect(
                        new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        BecomeCopyOfSubjectEffect.of(Subjects.exiledWithSourceUnlinked(
                                        new CardTypePredicate(CardType.CREATURE)))
                                .untilEndOfTurn()
                                .except(CopyExceptions.stillA(CardType.ARTIFACT)
                                        .alsoWithSubtypes(CardSubtype.VEHICLE))),
                "Tap two untapped creatures you control: Until end of turn, this Vehicle becomes a copy "
                        + "of the exiled card, except it's a Vehicle artifact in addition to its other types."
        ));
    }
}
