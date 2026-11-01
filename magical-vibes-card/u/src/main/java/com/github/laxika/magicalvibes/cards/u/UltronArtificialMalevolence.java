package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "252")
public class UltronArtificialMalevolence extends Card {

    public UltronArtificialMalevolence() {
        addTrigger(Triggers.enters(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayPayManaEffect(
                        "{2}",
                        new CreateTokenCopyOfTargetPermanentEffect().withAnimationIfNotCreature(
                                new AnimatePermanentsEffect(2, 2,
                                        List.of(CardSubtype.ROBOT, CardSubtype.VILLAIN), Set.of(), null,
                                        Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.PERMANENT)),
                        "Pay {2} to create a token that's a copy of that artifact?"));
    }
}
