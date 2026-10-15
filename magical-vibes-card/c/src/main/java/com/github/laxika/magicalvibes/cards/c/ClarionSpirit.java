package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "6")
public class ClarionSpirit extends Card {

    public ClarionSpirit() {
        // Whenever you cast your second spell each turn, create a 1/1 white Spirit creature token with flying.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(new CreateTokenEffect("Spirit", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()))
        ));
    }
}
