package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "75")
public class GeneratedHorizons extends Card {

    public GeneratedHorizons() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                CardType.LAND,
                1,
                "Forest",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.FOREST),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(ManaAbilities.tapFor(ManaColor.GREEN)),
                false,
                false,
                false,
                0,
                Set.of()));
    }
}
