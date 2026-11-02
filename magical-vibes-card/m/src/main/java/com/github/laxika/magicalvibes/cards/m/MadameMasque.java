package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "104")
public class MadameMasque extends Card {

    public MadameMasque() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawDiscardAndConniveEffect());
        addTrigger(Triggers.youDrawSecondCardEachTurn(), new CreateTokenEffect(
                "Villain", 2, 1, CardColor.BLACK, List.of(CardSubtype.VILLAIN),
                Set.of(Keyword.MENACE), Set.of()));
    }
}
