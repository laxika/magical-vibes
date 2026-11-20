package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "12")
@CardRegistration(set = "SOC", collectorNumber = "62")
public class CeaselessConflict extends Card {

    public CeaselessConflict() {
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addSpellEffect(new CreateTokenEffect(CardType.CREATURE, new AmountThisWay(StepKind.DESTROY)
                .matching(new PermanentNotPredicate(new PermanentIsTokenPredicate()))
                .controlledByYou(), "Spirit", 3, 2,
                CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.SPIRIT),
                Set.of(), Set.of(), false, false, List.of(), List.of(), false, false, false, 0, Set.of()));
    }
}
