package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantConvokeToNextSpellThisTurnEffect;

@CardRegistration(set = "MOC", collectorNumber = "41")
@CardRegistration(set = "MOC", collectorNumber = "128")
public class FlockchaserPhantom extends Card {

    public FlockchaserPhantom() {
        addEffect(EffectSlot.ON_ATTACK, new GrantConvokeToNextSpellThisTurnEffect());
    }
}
