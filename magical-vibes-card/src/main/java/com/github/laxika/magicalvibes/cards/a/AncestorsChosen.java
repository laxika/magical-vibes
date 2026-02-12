package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;

import java.util.List;
import java.util.Set;

public class AncestorsChosen extends Card {

    public AncestorsChosen() {
        super("Ancestor's Chosen", CardType.CREATURE, "{5}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.CLERIC));
        setCardText("First strike\nWhen Ancestor's Chosen enters the battlefield, you gain 1 life for each card in your graveyard.");
        setKeywords(Set.of(Keyword.FIRST_STRIKE));
        setPower(4);
        setToughness(4);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifePerGraveyardCardEffect());
    }
}
