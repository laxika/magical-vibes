package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BOT", collectorNumber = "2")
@CardRegistration(set = "BOT", collectorNumber = "17")
public class RatchetFieldMedic extends Card {

    public RatchetFieldMedic() {
        setBackFaceCard(new RatchetRescueRacer());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{1}{W}"));

        ReturnCardFromGraveyardEffect returnArtifact = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ARTIFACT))
                .targetGraveyard(true)
                .maxManaValueEqualsLifeGainedThisTurn(true)
                .enterTapped(true)
                .build();
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new MayEffect(
                        new TransformSelfThenEffect(new QueueReflexiveAbilityEffect(returnArtifact)),
                        "Convert Ratchet?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "RatchetRescueRacer";
    }
}
