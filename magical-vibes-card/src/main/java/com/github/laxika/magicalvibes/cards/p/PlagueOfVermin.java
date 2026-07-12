package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyLifeForTokensEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "73")
public class PlagueOfVermin extends Card {

    public PlagueOfVermin() {
        addEffect(EffectSlot.SPELL, new EachPlayerPaysAnyLifeForTokensEffect(
                new CreateTokenEffect("Rat", 1, 1, CardColor.BLACK, List.of(CardSubtype.RAT), Set.of(), Set.of())));
    }
}
