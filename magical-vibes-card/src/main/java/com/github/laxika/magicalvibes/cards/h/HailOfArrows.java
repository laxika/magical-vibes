package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;

import java.util.List;

public class HailOfArrows extends Card {

    public HailOfArrows() {
        super("Hail of Arrows", CardType.INSTANT, "{X}{W}", CardColor.WHITE);

        setCardText("Hail of Arrows deals X damage divided as you choose among any number of target attacking creatures.");
        addEffect(EffectSlot.SPELL, new DealXDamageDividedAmongTargetAttackingCreaturesEffect());
    }
}
