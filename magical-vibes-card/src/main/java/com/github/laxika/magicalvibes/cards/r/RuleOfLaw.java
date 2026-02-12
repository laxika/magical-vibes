package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;

public class RuleOfLaw extends Card {

    public RuleOfLaw() {
        super("Rule of Law", CardType.ENCHANTMENT, "{2}{W}", CardColor.WHITE);

        setCardText("Each player can't cast more than one spell each turn.");
        addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(1));
    }
}
