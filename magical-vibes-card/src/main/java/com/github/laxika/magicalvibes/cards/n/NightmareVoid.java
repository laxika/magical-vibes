package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "100")
public class NightmareVoid extends Card {

    public NightmareVoid() {
        addEffect(EffectSlot.SPELL,
                new ChooseCardsFromTargetHandEffect(1, List.of(), HandChoiceDestination.DISCARD));
        addEffect(EffectSlot.GRAVEYARD_DRAW_REPLACEMENT, new DredgeEffect(2));
    }
}
