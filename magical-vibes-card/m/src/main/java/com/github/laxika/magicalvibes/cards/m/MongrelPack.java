package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "237")
public class MongrelPack extends Card {

    public MongrelPack() {
        // When this creature dies during combat, create four 1/1 green Dog creature tokens.
        // The intervening-if reads the current step, which is still a combat step when the death
        // trigger resolves.
        addEffect(EffectSlot.ON_DEATH, new ConditionalEffect(new DuringCombat(),
                new CreateTokenEffect(4, "Dog", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.DOG), Set.of(), Set.of())));
    }
}
