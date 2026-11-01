package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "14")
@CardRegistration(set = "LCC", collectorNumber = "132")
public class MartyrOfDusk extends Card {

    public MartyrOfDusk() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                "Vampire",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.VAMPIRE),
                Set.of(Keyword.LIFELINK),
                Set.of()
        ));
    }
}
