package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "32")
@CardRegistration(set = "TDC", collectorNumber = "72")
public class WithinRange extends Card {

    public WithinRange() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Warrior", 1, 1, CardColor.RED,
                        List.of(CardSubtype.WARRIOR), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.DEFENDING_PLAYER));
    }
}
