package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEscapeToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

public class JurassicPark extends Card {

    public JurassicPark() {
        addEffect(EffectSlot.STATIC,
                new GrantEscapeToGraveyardCardsEffect(new CardSubtypePredicate(CardSubtype.DINOSAUR)));
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.GREEN,
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR),
                                CountScope.CONTROLLER))),
                "{T}: Add {G} for each Dinosaur you control."
        ));
    }
}
