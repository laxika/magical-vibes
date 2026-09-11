package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "101")
public class HydraTroopers extends Card {

    public HydraTroopers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalReplacementEffect(
                new GraveyardCardThreshold(2, new CardTypePredicate(CardType.CREATURE)),
                new MillEffect(2, MillRecipient.CONTROLLER),
                new CreateTokenEffect(
                        1, "Villain", 2, 1, CardColor.BLACK,
                        List.of(CardSubtype.VILLAIN), Set.of(Keyword.MENACE), Set.of(), true)));
    }
}
