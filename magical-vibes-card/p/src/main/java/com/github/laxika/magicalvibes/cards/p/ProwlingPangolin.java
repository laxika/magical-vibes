package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "163")
@CardRegistration(set = "EMA", collectorNumber = "104")
public class ProwlingPangolin extends Card {

    public ProwlingPangolin() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                        new SacrificePermanentsEffect(2,
                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                                Subjects.acceptedPlayers()),
                        "Sacrifice two creatures? If a player does, it is sacrificed.",
                        new SacrificeSelfEffect(), null));
    }
}
