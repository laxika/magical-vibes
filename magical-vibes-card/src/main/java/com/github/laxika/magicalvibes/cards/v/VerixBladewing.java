package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "149")
public class VerixBladewing extends Card {

    public VerixBladewing() {
        // Kicker {3}
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}"));

        // When Verix Bladewing enters, if it was kicked, create Karox Bladewing,
        // a legendary 4/4 red Dragon creature token with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KickedConditionalEffect(
                new CreateTokenEffect(CardType.CREATURE, 1, "Karox Bladewing", 4, 4,
                        CardColor.RED, null, List.of(CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), Set.of(),
                        false, false, Map.of(), List.of(), false, false, true)
        ));
    }
}
