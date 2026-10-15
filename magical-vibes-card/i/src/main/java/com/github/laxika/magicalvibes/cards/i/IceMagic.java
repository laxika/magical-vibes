package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TieredManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "56")
public class IceMagic extends Card {

    public IceMagic() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Blizzard - Return target creature to its owner's hand",
                        ReturnToHandEffect.target(), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Blizzara - Put target creature on its owner's library at a position from the top, then its owner chooses top or bottom",
                        new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Blizzaga - Its owner shuffles target creature into their library",
                        new ShuffleTargetPermanentIntoLibraryEffect(), TargetFilters.creature())
        )));
        addEffect(EffectSlot.SPELL, new TieredManaCost(List.of("", "{2}", "{5}{U}")));
    }
}
