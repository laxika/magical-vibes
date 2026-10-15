package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "60")
public class GlasspoolMimic extends Card {

    public GlasspoolMimic() {
        GlasspoolShore backFace = new GlasspoolShore();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "creature you control",
                Set.of(CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE),
                Map.of()
        ));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Glasspool Mimic", List.of()),
                new ChooseOneEffect.ChooseOneOption("Glasspool Shore", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "GlasspoolShore";
    }
}
