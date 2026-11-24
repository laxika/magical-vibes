package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "13")
@CardRegistration(set = "BLC", collectorNumber = "49")
public class TemptWithBunnies extends Card {

    private static final CreateTokenEffect RABBIT = new CreateTokenEffect("Rabbit", 1, 1, CardColor.WHITE,
            List.of(CardSubtype.RABBIT), Set.of(), Set.of());

    public TemptWithBunnies() {
        addSpellEffect(drawAndRabbit(Subjects.you()));
        addSpellEffect(TemptingOfferEffect.of(
                "Draw a card and create a 1/1 white Rabbit creature token? If you do, its controller draws a card "
                        + "and creates another one.",
                TemptWithBunnies::drawAndRabbit));
    }

    /** "[players] draw a card and create a 1/1 white Rabbit creature token". */
    private static CardEffect drawAndRabbit(PlayerSubject players) {
        return SequenceEffect.of(new DrawCardEffect(1, players), RABBIT.forController(players));
    }
}
