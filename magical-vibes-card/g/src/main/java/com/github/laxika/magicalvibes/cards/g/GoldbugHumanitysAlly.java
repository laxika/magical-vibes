package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToAttackingCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOT", collectorNumber = "11")
@CardRegistration(set = "BOT", collectorNumber = "25")
public class GoldbugHumanitysAlly extends Card {

    public GoldbugHumanitysAlly() {
        setBackFaceCard(new GoldbugScrappyScout());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{W}{U}"));

        addEffect(EffectSlot.STATIC,
                new PreventCombatDamageToAttackingCreaturesYouControlEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new NthSpellCastTriggerEffect(2, List.of(new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "GoldbugScrappyScout";
    }
}
