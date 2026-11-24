package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilityKind;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.PayZeroRatherThanAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "AFR", collectorNumber = "219")
public class BruenorBattlehammer extends Card {

    public BruenorBattlehammer() {
        addStaticEffect(new DynamicStaticBoostEffect(
                new Scaled(new AttachmentsOnSource(false, true), 2),
                new Fixed(0),
                Subjects.creaturesYouControl(),
                null,
                true));
        addStaticEffect(PayZeroRatherThanAbilityCostEffect.firstEachTurn(ActivatedAbilityKind.EQUIP));
    }
}
