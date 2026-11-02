package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "120")
public class DarettiRocketeerEngineer extends Card {

    public DarettiRocketeerEngineer() {
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate()), new Fixed(5)));

        ReturnCardFromGraveyardEffect returnArtifact = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.ARTIFACT))
                .targetGraveyard(true)
                .build();
        SacrificePermanentThenEffect sacrificeThenReturn = new SacrificePermanentThenEffect(
                new PermanentIsArtifactPredicate(), returnArtifact, "an artifact", true, false);
        MayEffect ability = new MayEffect(sacrificeThenReturn, "Sacrifice an artifact?");

        addTrigger(Triggers.enters(ObjectRelation.THIS), ability);
        addTrigger(Triggers.attacks(), ability);
    }
}
