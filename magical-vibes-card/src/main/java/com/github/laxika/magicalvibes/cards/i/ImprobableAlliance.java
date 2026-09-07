package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "193")
public class ImprobableAlliance extends Card {

    public ImprobableAlliance() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new CreateTokenEffect("Faerie", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.FAERIE), Set.of(Keyword.FLYING), Set.of()));

        addActivatedAbility(new ActivatedAbility(false, "{4}{U}{R}",
                List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{4}{U}{R}: Draw a card, then discard a card."));
    }
}
