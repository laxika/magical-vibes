package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "131")
public class HarriedDronesmith extends Card {

    public HarriedDronesmith() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Thopter",
                1,
                1,
                null,
                Set.of(),
                List.of(CardSubtype.THOPTER),
                Set.of(Keyword.FLYING),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of(Keyword.HASTE)));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
