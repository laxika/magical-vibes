package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "108")
@CardRegistration(set = "THB", collectorNumber = "315")
@CardRegistration(set = "DSC", collectorNumber = "149")
public class NightmareShepherd extends Card {

    public NightmareShepherd() {
        // "You may exile it. If you do, create a token that's a copy of that creature": the copy is of the creature as
        // it last existed on the battlefield (its ruling, CR 707.2).
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new MayEffect(
                SequenceEffect.of(
                        ExileGraveyardCardsEffect.cards(Subjects.triggeringCards()),
                        new ConditionalEffect(HappenedThisWay.exiled(),
                                new CreateTokenCopyOfTargetPermanentEffect(
                                        List.of(CardSubtype.NIGHTMARE), Set.of(), 1, 1, Map.of())
                                        .ofTriggeringPermanent())),
                "Exile that creature to create a 1/1 Nightmare token copy?"));
    }
}
