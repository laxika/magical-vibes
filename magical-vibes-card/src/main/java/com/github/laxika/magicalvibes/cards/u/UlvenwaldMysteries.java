package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "222")
public class UlvenwaldMysteries extends Card {

    public UlvenwaldMysteries() {
        // Whenever a nontoken creature you control dies, investigate.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, CreateTokenEffect.ofClueToken(1));

        // Whenever you sacrifice a Clue, create a 1/1 white Human Soldier creature token.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                        new CreateTokenEffect("Human Soldier", 1, 1,
                                CardColor.WHITE, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                Set.of(), Set.of())
                ));
    }
}
