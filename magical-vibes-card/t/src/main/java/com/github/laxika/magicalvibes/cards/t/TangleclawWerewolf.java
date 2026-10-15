package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FibrousEntangler;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "174")
public class TangleclawWerewolf extends Card {

    public TangleclawWerewolf() {
        setBackFaceCard(new FibrousEntangler());

        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{G}",
                List.of(new TransformSelfEffect()),
                "{6}{G}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "FibrousEntangler";
    }
}
