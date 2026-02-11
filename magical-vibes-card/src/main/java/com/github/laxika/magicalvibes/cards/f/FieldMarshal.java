package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;

import java.util.List;
import java.util.Set;

public class FieldMarshal extends Card {

    public FieldMarshal() {
        super("Field Marshal", CardType.CREATURE, "{1}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER));
        setCardText("Other Soldier creatures get +1/+1 and have first strike.");
        setPower(2);
        setToughness(2);
        setStaticEffects(List.of(new BoostCreaturesBySubtypeEffect(Set.of(CardSubtype.SOLDIER), 1, 1, Set.of(Keyword.FIRST_STRIKE))));
    }
}
