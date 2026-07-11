package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "138")
public class WalkerOfTheGrove extends Card {

    public WalkerOfTheGrove() {
        // "When this creature leaves the battlefield, create a 4/4 green Elemental creature token."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new CreateTokenEffect("Elemental", 4, 4, CardColor.GREEN,
                        List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of()));

        // Evoke {4}{G}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{G}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
