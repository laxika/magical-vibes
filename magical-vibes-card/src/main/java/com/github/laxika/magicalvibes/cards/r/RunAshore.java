package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "74")
public class RunAshore extends Card {

    public RunAshore() {
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "The owner of target nonland permanent puts it on the top or bottom of their library",
                        new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0),
                        TargetFilters.nonlandPermanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target nonland permanent to its owner's hand",
                        ReturnToHandEffect.target(),
                        TargetFilters.nonlandPermanent())
        )));
    }
}
