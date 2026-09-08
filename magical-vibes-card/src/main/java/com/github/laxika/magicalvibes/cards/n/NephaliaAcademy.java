package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DiscardToTopOfLibraryInsteadEffect;

@CardRegistration(set = "EMN", collectorNumber = "205")
public class NephaliaAcademy extends Card {

    public NephaliaAcademy() {
        addEffect(EffectSlot.STATIC, new DiscardToTopOfLibraryInsteadEffect(true));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
