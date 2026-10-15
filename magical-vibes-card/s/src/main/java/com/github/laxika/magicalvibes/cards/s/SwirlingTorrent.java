package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "56")
public class SwirlingTorrent extends Card {

    public SwirlingTorrent() {
        setAllowSharedTargets(true);

        TargetFilter creature = TargetFilters.creature();
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put target creature on top of its owner's library",
                        new PutTargetOnTopOfLibraryEffect(),
                        creature),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature to its owner's hand",
                        ReturnToHandEffect.target(),
                        creature)
        )));
    }
}
