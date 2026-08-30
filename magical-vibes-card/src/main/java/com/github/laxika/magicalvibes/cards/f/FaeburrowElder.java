package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.AwardOneManaOfEachColorAmongControlledEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "190")
public class FaeburrowElder extends Card {

    public FaeburrowElder() {
        ColorsAmongControlledPermanents colors = new ColorsAmongControlledPermanents();
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(colors, colors));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardOneManaOfEachColorAmongControlledEffect(new PermanentTruePredicate())),
                "{T}: For each color among permanents you control, add one mana of that color."
        ));
    }
}
