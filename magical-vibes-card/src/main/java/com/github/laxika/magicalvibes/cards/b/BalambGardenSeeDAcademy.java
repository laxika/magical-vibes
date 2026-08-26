package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "272")
public class BalambGardenSeeDAcademy extends Card {

    public BalambGardenSeeDAcademy() {
        setBackFaceCard(new BalambGardenAirborne());

        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {G} or {U}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{G}{U}",
                List.of(
                        new ReduceActivationCostEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.TOWN),
                                CountScope.CONTROLLER,
                                true)),
                        new TransformSelfEffect()
                ),
                "{5}{G}{U}, {T}: Transform this land. This ability costs {1} less to activate for each other Town you control."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "BalambGardenAirborne";
    }
}
