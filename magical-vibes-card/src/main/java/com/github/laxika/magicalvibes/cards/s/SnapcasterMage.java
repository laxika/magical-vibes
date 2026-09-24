package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "78")
@CardRegistration(set = "INR", collectorNumber = "478")
@CardRegistration(set = "MM3", collectorNumber = "50")
@CardRegistration(set = "SLD", collectorNumber = "808")
public class SnapcasterMage extends Card {

    public SnapcasterMage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantFlashbackToTargetGraveyardCardEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
