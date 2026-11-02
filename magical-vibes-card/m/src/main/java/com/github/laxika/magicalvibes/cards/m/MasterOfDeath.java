package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "205")
@CardRegistration(set = "MKC", collectorNumber = "216")
public class MasterOfDeath extends Card {

    public MasterOfDeath() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(2));
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceCardInGraveyard(),
                        new MayPayLifeEffect(
                                1,
                                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "Pay 1 life to return Master of Death to your hand?")));
    }
}
