package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsDuringCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfThenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOT", collectorNumber = "12")
@CardRegistration(set = "BOT", collectorNumber = "26")
public class MegatronTyrant extends Card {

    public MegatronTyrant() {
        setBackFaceCard(new MegatronDestructiveForce());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{1}{R}{W}{B}"));

        addStaticEffect(new OpponentsCantCastSpellsDuringCombatEffect());
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU),
                new MayEffect(
                        new TransformSelfThenEffect(
                                new AwardManaEffect(ManaColor.COLORLESS,
                                        new LifeLostThisTurn(CountScope.OPPONENTS))),
                        "Convert Megatron?",
                        null,
                        MayChoicePlayer.CONTROLLER));
    }

    @Override
    public String getBackFaceClassName() {
        return "MegatronDestructiveForce";
    }
}
