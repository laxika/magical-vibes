package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "48")
@CardRegistration(set = "NCC", collectorNumber = "148")
public class LifeOfTheParty extends Card {

    public LifeOfTheParty() {
        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addTrigger(Triggers.attacks(), BoostEffect.self(creaturesYouControl, new Fixed(0)));

        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new CreateTokenCopyOfSourceForEachOtherPlayerEffect(
                        CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(false, List.of())),
                GoadTargetCreatureUntilNextTurnEffect.of(Subjects.createdThisResolution(), Duration.PERMANENT)));
    }
}
