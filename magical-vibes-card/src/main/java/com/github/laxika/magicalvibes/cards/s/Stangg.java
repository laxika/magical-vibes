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

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "CHR", collectorNumber = "86")
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
                Map.of(),
                List.of(),
                false,
                false,
                true,
                0,
                Set.of());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenAndLinkToSourceEffect(twin));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ExileTokensCreatedWithSourceEffect());
    }
}
