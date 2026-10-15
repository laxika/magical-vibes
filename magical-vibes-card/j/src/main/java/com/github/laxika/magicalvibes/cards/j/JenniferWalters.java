package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheSensationalSheHulk;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "18")
public class JenniferWalters extends Card {

    public JenniferWalters() {
        setBackFaceCard(new TheSensationalSheHulk());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.STATIC, new OpponentsCantCastOrActivateDuringYourTurnEffect(false));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{W}{W}",
                List.of(new TransformSelfEffect()),
                "{3}{G}{W}{W}: Transform Jennifer Walters. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Jennifer Walters", List.of())
                        .withManaCost("{1}{W}"),
                new ChooseOneEffect.ChooseOneOption("The Sensational She-Hulk", List.of())
                        .withManaCost("{3}{G}{W}{W}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheSensationalSheHulk";
    }
}
