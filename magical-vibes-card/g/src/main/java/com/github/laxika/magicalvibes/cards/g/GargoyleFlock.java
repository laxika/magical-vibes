package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "123")
public class GargoyleFlock extends Card {

    public GargoyleFlock() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE), 1),
                new CreateTokenEffect(
                        "Tyranid Gargoyle", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.TYRANID, CardSubtype.GARGOYLE),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
