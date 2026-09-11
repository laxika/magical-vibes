package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "83")
public class YouComeToARiver extends Card {

    public YouComeToARiver() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target nonland permanent to its owner's hand",
                        ReturnToHandEffect.target(),
                        TargetFilters.nonlandPermanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +1/+0 until end of turn and can't be blocked this turn",
                        List.of(new BoostTargetCreatureEffect(1, 0), new MakeCreatureUnblockableEffect()),
                        TargetFilters.creature())
        )));
    }
}
