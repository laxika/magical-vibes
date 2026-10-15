package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensWithCountersFromControlledCreaturesEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "185")
public class ExoticPets extends Card {

    public ExoticPets() {
        CreateTokenEffect fish = new CreateTokenEffect(
                2,
                "Fish",
                1,
                1,
                CardColor.BLUE,
                List.of(CardSubtype.FISH),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CantBeBlockedEffect())
        );

        addEffect(EffectSlot.SPELL, new CreateTokensWithCountersFromControlledCreaturesEffect(fish));
    }
}
