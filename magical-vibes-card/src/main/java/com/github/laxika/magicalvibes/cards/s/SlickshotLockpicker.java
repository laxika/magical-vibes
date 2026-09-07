package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "67")
public class SlickshotLockpicker extends Card {

    public SlickshotLockpicker() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{U}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantFlashbackToTargetGraveyardCardEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
