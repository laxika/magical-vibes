package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ECC", collectorNumber = "23")
@CardRegistration(set = "ECC", collectorNumber = "43")
public class RainSlickedCopse extends Card {

    public RainSlickedCopse() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {G} or {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        // Cycling {2} ({2}, Discard this card: Draw a card.)
        addCycling("{2}");
    }
}
