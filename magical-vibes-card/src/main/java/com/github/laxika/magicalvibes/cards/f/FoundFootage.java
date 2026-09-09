package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "246")
public class FoundFootage extends Card {

    public FoundFootage() {
        addEffect(EffectSlot.STATIC, new LookAtFaceDownCreaturesEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new SurveilEffect(2), new DrawCardEffect()),
                "{2}, Sacrifice this artifact: Surveil 2, then draw a card."
        ));
    }
}
