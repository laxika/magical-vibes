package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "25")
public class MuseumNightwatch extends Card {

    public MuseumNightwatch() {
        addMorph("{1}{W}");
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                1,
                "Detective",
                2,
                2,
                CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLUE),
                List.of(CardSubtype.DETECTIVE)));
    }
}
