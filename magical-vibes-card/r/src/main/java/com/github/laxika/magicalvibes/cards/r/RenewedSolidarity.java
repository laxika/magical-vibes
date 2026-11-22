package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "DRC", collectorNumber = "7")
@CardRegistration(set = "DRC", collectorNumber = "23")
public class RenewedSolidarity extends Card {

    public RenewedSolidarity() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(new BoostCreaturesOfChosenSubtypeEffect(1, 0));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new CreateTokenCopyOfTargetPermanentEffect().withSubject(Subjects.controlledBy(Subjects.you(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsTokenPredicate(),
                                new PermanentHasSourceChosenSubtypePredicate(),
                                new PermanentEnteredBattlefieldThisTurnPredicate())))));
    }
}
