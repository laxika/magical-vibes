package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHR", collectorNumber = "86")
@CardRegistration(set = "LEG", collectorNumber = "260")
@CardRegistration(set = "ME3", collectorNumber = "177")
@CardRegistration(set = "A25", collectorNumber = "218")
public class Stangg extends Card {

    public Stangg() {
        CreateTokenEffect twin = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Stangg Twin",
                3,
                4,
                CardColor.RED,
                Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.HUMAN, CardSubtype.WARRIOR),
                Set.of(),
                Set.of(),
                false,
                false,
                List.of(),
                List.of(),
                false,
                false,
                true,
                0,
                Set.of());

        // "Exile that token when Stangg leaves the battlefield. Sacrifice Stangg when that token leaves the
        // battlefield." Both are delayed triggered abilities created by the enters trigger, so they stay under
        // the enters trigger's controller even if Stangg changes controllers.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenAndLinkToSourceEffect(
                twin, false, new SacrificeSelfEffect(), new ExileTokensCreatedWithSourceEffect()));
    }
}
