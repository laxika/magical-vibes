package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "91")
public class BlackbloomRogue extends Card {

    public BlackbloomRogue() {
        setBackFaceCard(new BlackbloomBog());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentGraveyardAtLeast(8),
                new StaticBoostEffect(3, 0, GrantScope.SELF)));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Blackbloom Rogue", List.of()),
                new ChooseOneEffect.ChooseOneOption("Blackbloom Bog", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "BlackbloomBog";
    }
}
