package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextSpellOfTypeThisTurnEffect;

@CardRegistration(set = "M14", collectorNumber = "68")
@CardRegistration(set = "GPT", collectorNumber = "31")
public class Quicken extends Card {

    public Quicken() {
        addEffect(EffectSlot.SPELL, new GrantFlashToNextSpellOfTypeThisTurnEffect(CardType.SORCERY));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
