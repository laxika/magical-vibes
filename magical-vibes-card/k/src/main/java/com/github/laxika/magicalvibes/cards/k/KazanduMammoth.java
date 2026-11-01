package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "189")
public class KazanduMammoth extends Card {

    public KazanduMammoth() {
        setBackFaceCard(new KazanduValley());
        setModalDoubleFaced(true);

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Kazandu Mammoth", List.of()),
                new ChooseOneEffect.ChooseOneOption("Kazandu Valley", List.of())
        )));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new BoostSelfEffect(2, 2));
    }

    @Override
    public String getBackFaceClassName() {
        return "KazanduValley";
    }
}
