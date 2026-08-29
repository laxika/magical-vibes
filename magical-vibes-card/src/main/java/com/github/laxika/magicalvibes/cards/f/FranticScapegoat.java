package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsSuspected;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectChosenOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;

@CardRegistration(set = "MKM", collectorNumber = "126")
@CardRegistration(set = "MKM", collectorNumber = "347")
public class FranticScapegoat extends Card {

    public FranticScapegoat() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SuspectEffect(GrantScope.SELF));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, suspectAnotherCreature());
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD, suspectAnotherCreature());
    }

    private MayEffect suspectAnotherCreature() {
        return new MayEffect(
                new ConditionalEffect(new SourceIsSuspected(), new SuspectChosenOtherCreatureEffect()),
                "Suspect another creature and stop suspecting this creature?");
    }
}
