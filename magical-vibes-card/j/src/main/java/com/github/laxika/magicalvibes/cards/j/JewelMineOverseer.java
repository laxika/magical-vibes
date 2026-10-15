package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;
import java.util.Map;

@CardRegistration(set = "YWOE", collectorNumber = "21")
public class JewelMineOverseer extends Card {

    public JewelMineOverseer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ConjureCardNamedIntoLibraryEffect(
                        "Seven Dwarves", 7,
                        Map.of(EffectSlot.ON_ENTER_BATTLEFIELD, List.of(new DrawCardEffect(1)))),
                new ShuffleLibraryEffect(false)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
