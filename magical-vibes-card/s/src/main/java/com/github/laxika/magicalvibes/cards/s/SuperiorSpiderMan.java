package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;


@CardRegistration(set = "SPM", collectorNumber = "155")
@CardRegistration(set = "SPM", collectorNumber = "275")
@CardRegistration(set = "OM1", collectorNumber = "140")
public class SuperiorSpiderMan extends Card {

    public SuperiorSpiderMan() {
        addStaticEffect(CopyCreatureCardInGraveyardOnEnterEffect.of(
                        Subjects.cardsInGraveyards(Subjects.eachPlayer(), new CardTypePredicate(CardType.CREATURE)))
                .except(CopyExceptions.named("Superior Spider-Man")
                        .alsoWithBasePowerToughness(4, 4)
                        .alsoWithSubtypes(CardSubtype.SPIDER, CardSubtype.HUMAN, CardSubtype.HERO))
                .exilingCopiedCard());
    }
}
