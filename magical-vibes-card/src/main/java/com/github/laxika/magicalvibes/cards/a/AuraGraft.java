package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;

public class AuraGraft extends Card {

    public AuraGraft() {
        super("Aura Graft", CardType.INSTANT, "{1}{U}", CardColor.BLUE);

        setCardText("Gain control of target Aura that's attached to a permanent. Attach it to another permanent it can enchant.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new GainControlOfTargetAuraEffect());
    }
}
