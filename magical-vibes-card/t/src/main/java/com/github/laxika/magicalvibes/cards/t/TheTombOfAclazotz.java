package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class TheTombOfAclazotz extends Card {

    public TheTombOfAclazotz() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AllowCastMatchingCardsFromGraveyardThisTurnEffect.oneShotWithEntryEffects(
                        new CardTypePredicate(CardType.CREATURE), CounterType.FINALITY, CardSubtype.VAMPIRE)),
                "{T}: You may cast a creature spell from your graveyard this turn. If you do, it enters with a finality counter on it and is a Vampire in addition to its other types."));
    }
}
