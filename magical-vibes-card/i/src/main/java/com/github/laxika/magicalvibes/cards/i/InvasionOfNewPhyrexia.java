package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TeferiAkosaOfZhalfir;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "239")
public class InvasionOfNewPhyrexia extends Card {

    public InvasionOfNewPhyrexia() {
        setBackFaceCard(new TeferiAkosaOfZhalfir());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                CardType.CREATURE,
                new XValue(),
                "Knight",
                2,
                2,
                CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLUE),
                List.of(CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE),
                Set.of(),
                false,
                false,
                List.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "TeferiAkosaOfZhalfir";
    }
}
