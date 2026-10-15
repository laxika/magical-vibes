package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "97")
public class FeralLightning extends Card {

    public FeralLightning() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                3,
                "Elemental",
                3,
                1,
                CardColor.RED,
                null,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.HASTE),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                true,
                false,
                0,
                Set.of()));
    }
}
