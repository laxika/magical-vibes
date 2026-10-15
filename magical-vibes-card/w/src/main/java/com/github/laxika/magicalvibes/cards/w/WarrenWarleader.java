package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "38")
public class WarrenWarleader extends Card {

    public WarrenWarleader() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new CreateTokenCopyOfSourceEffect(false, 1, null, null, false, 1, 1)));
        addEffect(EffectSlot.ON_ATTACK, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 white Rabbit creature token that's tapped and attacking.",
                        List.of(
                                new CreateTokenEffect(1, "Rabbit", 1, 1, CardColor.WHITE,
                                        List.of(CardSubtype.RABBIT), Set.of(), Set.of(), true),
                                new MakeCreatedPermanentsAttackingEffect())),
                new ChooseOneEffect.ChooseOneOption(
                        "Attacking creatures you control get +1/+1 until end of turn.",
                        new BoostAllOwnCreaturesEffect(1, 1, new PermanentIsAttackingPredicate()))
        )));
    }
}
