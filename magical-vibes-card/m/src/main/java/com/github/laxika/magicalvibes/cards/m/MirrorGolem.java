package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromImprintedCardTypesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "208")
public class MirrorGolem extends Card {

    public MirrorGolem() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true).imprintingOnSource(),
                "Exile target card from a graveyard?"));
        addStaticEffect(new ProtectionFromImprintedCardTypesEffect());
    }
}
