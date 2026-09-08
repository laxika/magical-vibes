package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "80")
public class CryptcallerChariot extends Card {

    public CryptcallerChariot() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                new CreateTokenEffect(
                        CardType.CREATURE,
                        new EventValue(),
                        "Zombie",
                        2,
                        2,
                        CardColor.BLACK,
                        null,
                        List.of(CardSubtype.ZOMBIE),
                        Set.of(),
                        Set.of(),
                        false,
                        true,
                        Map.of(),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"));
    }
}
