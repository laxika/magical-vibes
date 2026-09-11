package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PC2", collectorNumber = "87")
public class DragonlairSpider extends Card {

    public DragonlairSpider() {
        // Whenever an opponent casts a spell, create a 1/1 green Insect creature token.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new CreateTokenEffect(
                        "Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of()))));
    }
}
