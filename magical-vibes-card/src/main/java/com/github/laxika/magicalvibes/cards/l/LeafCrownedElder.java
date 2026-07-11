package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "128")
public class LeafCrownedElder extends Card {

    public LeafCrownedElder() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, you may
        // play that card without paying its mana cost. (If not played, it stays on top — no exile.)
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new KinshipEffect(List.of(
                new RevealTopCardMayPlayFreeOrExileEffect(false))));
    }
}
