package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "44")
public class RetreatToEmeria extends Card {

    public RetreatToEmeria() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Create a 1/1 white Kor Ally creature token.",
                                new CreateTokenEffect("Kor Ally", 1, 1, CardColor.WHITE,
                                        List.of(CardSubtype.KOR, CardSubtype.ALLY), Set.of(), Set.of())),
                        new ChooseOneEffect.ChooseOneOption(
                                "Creatures you control get +1/+1 until end of turn.",
                                new BoostAllOwnCreaturesEffect(1, 1))
                )));
    }
}
