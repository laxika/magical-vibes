package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "62")
public class SkystrikeOfficer extends Card {

    public SkystrikeOfficer() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(1, "Soldier", 1, 1, null,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.SOLDIER)),
                        new DrawCardEffect(1)
                ),
                "Tap three untapped Soldiers you control: Draw a card."
        ));
    }
}
