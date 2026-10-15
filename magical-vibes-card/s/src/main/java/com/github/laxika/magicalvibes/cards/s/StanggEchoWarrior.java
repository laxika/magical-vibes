package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfAttachedAurasAndEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "42")
@CardRegistration(set = "DMC", collectorNumber = "64")
public class StanggEchoWarrior extends Card {

    public StanggEchoWarrior() {
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
                true,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                true,
                0,
                Set.of());

        addEffect(EffectSlot.ON_ATTACK, twin);
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenCopiesOfAttachedAurasAndEquipmentEffect());
        addEffect(EffectSlot.ON_ATTACK, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
