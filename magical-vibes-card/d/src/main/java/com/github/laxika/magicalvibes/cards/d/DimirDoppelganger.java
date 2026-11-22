package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RAV", collectorNumber = "202")
@CardRegistration(set = "GK1", collectorNumber = "12")
public class DimirDoppelganger extends Card {

    public DimirDoppelganger() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}{B}",
                List.of(
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                                1, new CardTypePredicate(CardType.CREATURE), false),
                        BecomeCopyOfSubjectEffect.of(Subjects.cardsThisWay(StepKind.EXILE)).keepingThisAbility()),
                "Exile target creature card from a graveyard. This creature becomes a copy of that card, except it has this ability."));
    }
}
