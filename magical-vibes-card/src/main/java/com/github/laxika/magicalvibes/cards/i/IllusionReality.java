package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "129")
public class IllusionReality extends Card {

    public IllusionReality() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Illusion — Target spell or permanent becomes the color of your choice until end of turn",
                        new SetChosenColorUntilEndOfTurnEffect(true)
                ).withManaCost("{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Reality — Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()
                ).withManaCost("{2}{G}")
        )));
    }
}
