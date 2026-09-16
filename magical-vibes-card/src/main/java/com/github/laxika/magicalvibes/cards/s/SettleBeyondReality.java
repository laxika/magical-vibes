package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "27")
@CardRegistration(set = "2X2", collectorNumber = "30")
public class SettleBeyondReality extends Card {

    public SettleBeyondReality() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature you don't control",
                        new ExileTargetPermanentEffect(),
                        TargetFilters.creatureAnOpponentControls()),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature you control, then return it to the battlefield under its owner's control",
                        FlickerEffect.flickerTarget(),
                        TargetFilters.creatureYouControl())
        )));
    }
}
