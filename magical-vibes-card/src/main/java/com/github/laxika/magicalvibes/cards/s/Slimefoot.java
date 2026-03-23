package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "205")
public class Slimefoot extends Card {

    public Slimefoot() {
        // Whenever a Saproling you control dies, Slimefoot, the Stowaway deals 1 damage to each
        // opponent and you gain 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new SubtypeConditionalEffect(CardSubtype.SAPROLING, new DealDamageToEachOpponentEffect(1)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new SubtypeConditionalEffect(CardSubtype.SAPROLING, new GainLifeEffect(1)));

        // {4}: Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(false, "{4}",
                List.of(new CreateTokenEffect("Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                "{4}: Create a 1/1 green Saproling creature token."));
    }
}
