package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheOmenkeel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfForVoyageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "50")
public class CosimaGodOfTheVoyage extends Card {

    public CosimaGodOfTheVoyage() {
        setBackFaceCard(new TheOmenkeel());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ExileSelfForVoyageEffect(), "Exile Cosima?"));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Cosima, God of the Voyage", List.of())
                        .withManaCost("{2}{U}"),
                new ChooseOneEffect.ChooseOneOption("The Omenkeel", List.of())
                        .withManaCost("{1}{U}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheOmenkeel";
    }
}
