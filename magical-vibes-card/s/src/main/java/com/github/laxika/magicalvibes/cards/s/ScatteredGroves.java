package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "AKH", collectorNumber = "247")
@CardRegistration(set = "AKR", collectorNumber = "327")
@CardRegistration(set = "WHO", collectorNumber = "892")
@CardRegistration(set = "WHO", collectorNumber = "1103")
@CardRegistration(set = "WHO", collectorNumber = "301")
@CardRegistration(set = "WHO", collectorNumber = "512")
@CardRegistration(set = "PIP", collectorNumber = "286")
@CardRegistration(set = "PIP", collectorNumber = "505")
@CardRegistration(set = "PIP", collectorNumber = "814")
@CardRegistration(set = "PIP", collectorNumber = "1033")
@CardRegistration(set = "LTC", collectorNumber = "328")
@CardRegistration(set = "MSC", collectorNumber = "262")
@CardRegistration(set = "MSC", collectorNumber = "490")
@CardRegistration(set = "MKC", collectorNumber = "286")
public class ScatteredGroves extends Card {

    public ScatteredGroves() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
