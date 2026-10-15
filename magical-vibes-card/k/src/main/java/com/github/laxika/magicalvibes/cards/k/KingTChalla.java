package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BlackPantherHopeEnduring;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "219")
public class KingTChalla extends Card {

    public KingTChalla() {
        BlackPantherHopeEnduring backFace = new BlackPantherHopeEnduring();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new NthCardDrawTriggerEffect(2, new DrawCardEffect(1)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{U}",
                List.of(new TransformSelfEffect()),
                "{4}{W}{U}: Transform King T'Challa. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("King T'Challa", List.of())
                        .withManaCost("{1}{W}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Black Panther, Hope Enduring", backFace.getEffects(EffectSlot.SPELL))
                        .withManaCost("{4}{W}{U}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "BlackPantherHopeEnduring";
    }
}
