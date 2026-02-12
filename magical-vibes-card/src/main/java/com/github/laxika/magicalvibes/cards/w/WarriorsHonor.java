package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

public class WarriorsHonor extends Card {

    public WarriorsHonor() {
        super("Warrior's Honor", CardType.INSTANT, "{2}{W}", CardColor.WHITE);

        setCardText("Creatures you control get +1/+1 until end of turn.");
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
