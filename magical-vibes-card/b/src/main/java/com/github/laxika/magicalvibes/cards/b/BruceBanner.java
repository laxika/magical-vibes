package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheIncredibleHulk;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "49")
public class BruceBanner extends Card {

    public BruceBanner() {
        setBackFaceCard(new TheIncredibleHulk());
        setModalDoubleFaced(true);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{X}",
                List.of(new DrawCardEffect(new XValue())),
                "{X}{X}, {T}: Draw X cards. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{R}{G}{G}",
                List.of(new TransformSelfEffect()),
                "{2}{R}{R}{G}{G}: Transform Bruce Banner. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Bruce Banner", List.of())
                        .withManaCost("{U}"),
                new ChooseOneEffect.ChooseOneOption("The Incredible Hulk", List.of())
                        .withManaCost("{2}{R}{R}{G}{G}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheIncredibleHulk";
    }
}
