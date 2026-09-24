package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentSubtypeAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "609")
public class HavengulLaboratory extends Card {

    public HavengulLaboratory() {
        setBackFaceCard(new HavengulMystery());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(CreateTokenEffect.ofClueToken(1)),
                "{4}, {T}: Investigate."
        ));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControllerSacrificedPermanentSubtypeAtLeastThisTurn(3, CardSubtype.CLUE),
                new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "HavengulMystery";
    }
}
