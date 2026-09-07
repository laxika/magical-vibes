package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "SNC", collectorNumber = "122")
public class RobTheArchives extends Card {

    public RobTheArchives() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfCasualtyPaidEffect());
        addEffect(EffectSlot.SPELL, new CasualtyCost(1));
        addEffect(EffectSlot.SPELL, new ExileTopCardMayPlayThisTurnEffect(2, false));
    }
}
