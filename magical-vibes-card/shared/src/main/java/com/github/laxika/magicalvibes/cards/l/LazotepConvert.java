package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Back face of Invasion of Amonkhet. */
public class LazotepConvert extends Card {

    public LazotepConvert() {
        addStaticEffect(CopyCreatureCardInGraveyardOnEnterEffect.of(
                        Subjects.cardsInGraveyards(Subjects.eachPlayer(), new CardTypePredicate(CardType.CREATURE)))
                .except(CopyExceptions.withBasePowerToughness(4, 4)
                        .alsoColored(CardColor.BLACK)
                        .alsoWithSubtypes(CardSubtype.ZOMBIE)));
    }
}
