package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "46")
public class BeyeenVeil extends Card {

    public BeyeenVeil() {
        setBackFaceCard(new BeyeenCoast());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures your opponents control get -2/-0 until end of turn",
                        new BoostAllCreaturesEffect(-2, 0,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                new ChooseOneEffect.ChooseOneOption("Beyeen Coast", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "BeyeenCoast";
    }
}
