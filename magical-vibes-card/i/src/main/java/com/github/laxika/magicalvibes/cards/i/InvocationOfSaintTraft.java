package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "246")
public class InvocationOfSaintTraft extends Card {

    public InvocationOfSaintTraft() {
        // Enchanted creature has "Whenever this creature attacks, create a 4/4 white Angel
        // creature token with flying that's tapped and attacking. Exile that token at end of
        // combat."
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK, new CreateTokenForTriggeringPlayerEffect(
                        new CreateTokenEffect(1, "Angel", 4, 4, CardColor.WHITE,
                                List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), true, true)));
    }
}
