package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "119")
@CardRegistration(set = "WHO", collectorNumber = "405")
@CardRegistration(set = "WHO", collectorNumber = "724")
@CardRegistration(set = "WHO", collectorNumber = "996")
public class TheCyberController extends Card {

    public TheCyberController() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MillEachOpponentAndPutMilledCreaturesFaceDownAsCybermenEffect(new XValue()));
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentIsArtifactPredicate()));
    }
}
