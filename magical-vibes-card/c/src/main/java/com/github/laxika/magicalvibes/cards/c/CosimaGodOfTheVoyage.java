package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheOmenkeel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfForVoyageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "50")
@CardRegistration(set = "KHM", collectorNumber = "303")
public class CosimaGodOfTheVoyage extends Card {

    public CosimaGodOfTheVoyage() {
        setBackFaceCard(new TheOmenkeel());
        setModalDoubleFaced(true);

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new ExileSelfForVoyageEffect(), "Exile Cosima?"));
        addSpellEffect(new ChooseOneEffect(List.of(
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
