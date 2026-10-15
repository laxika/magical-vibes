package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EchoingEquation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "147")
public class AugmenterPugilist extends Card {

    public AugmenterPugilist() {
        EchoingEquation backFace = new EchoingEquation();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(8, new PermanentIsLandPredicate()),
                new StaticBoostEffect(5, 5, GrantScope.SELF)));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Augmenter Pugilist", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Echoing Equation", backFace.getEffects(EffectSlot.SPELL),
                        TargetFilters.creatureYouControl())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "EchoingEquation";
    }
}
