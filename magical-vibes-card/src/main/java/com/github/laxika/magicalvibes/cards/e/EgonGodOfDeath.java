package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.ThroneOfDeath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "92")
public class EgonGodOfDeath extends Card {

    public EgonGodOfDeath() {
        setBackFaceCard(new ThroneOfDeath());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new ExileNCardsFromGraveyardCost(2, null),
                List.of(new SacrificeSelfEffect(), new DrawCardEffect(1))));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Egon, God of Death", List.of())
                        .withManaCost("{2}{B}"),
                new ChooseOneEffect.ChooseOneOption("Throne of Death", List.of())
                        .withManaCost("{B}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "ThroneOfDeath";
    }
}
