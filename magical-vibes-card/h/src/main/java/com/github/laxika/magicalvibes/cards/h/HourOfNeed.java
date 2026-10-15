package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "40")
public class HourOfNeed extends Card {

    public HourOfNeed() {
        // Strive — This spell costs {1}{U} more to cast for each target beyond the first.
        setAdditionalManaCostPerExtraTarget("{1}{U}");

        // Exile any number of target creatures. For each creature exiled this way, its controller
        // creates a 4/4 blue Sphinx creature token with flying.
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect(new CreateTokenEffect(
                        "Sphinx", 4, 4, CardColor.BLUE, List.of(CardSubtype.SPHINX),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
