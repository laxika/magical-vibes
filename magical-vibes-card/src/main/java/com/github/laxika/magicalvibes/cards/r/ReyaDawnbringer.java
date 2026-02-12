package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;

import java.util.List;
import java.util.Set;

public class ReyaDawnbringer extends Card {

    public ReyaDawnbringer() {
        super("Reya Dawnbringer", CardType.CREATURE, "{6}{W}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.ANGEL));
        setCardText("Flying\nAt the beginning of your upkeep, you may return target creature card from your graveyard to the battlefield.");
        setKeywords(Set.of(Keyword.FLYING));
        setUpkeepTriggeredEffects(List.of(new ReturnCreatureFromGraveyardToBattlefieldEffect()));
        setPower(4);
        setToughness(6);
    }
}
