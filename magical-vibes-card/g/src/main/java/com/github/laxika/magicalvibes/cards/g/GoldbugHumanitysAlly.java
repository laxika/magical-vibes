package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToAttackingCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOT", collectorNumber = "11")
@CardRegistration(set = "BOT", collectorNumber = "25")
public class GoldbugHumanitysAlly extends Card {

    public GoldbugHumanitysAlly() {
        setBackFaceCard(new GoldbugScrappyScout());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{W}{U}"));

        addStaticEffect(new PreventCombatDamageToAttackingCreaturesYouControlEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "GoldbugScrappyScout";
    }
}
