package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "137")
@CardRegistration(set = "KLD", collectorNumber = "5")
public class AuthorityOfTheConsuls extends Card {

    public AuthorityOfTheConsuls() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(Set.of(CardType.CREATURE), true));
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));
    }
}
