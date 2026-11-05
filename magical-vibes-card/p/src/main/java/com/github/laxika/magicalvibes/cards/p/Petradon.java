package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "108")
public class Petradon extends Card {

    public Petradon() {
        var exileTargetPermanentAndTrackWithSource = new ExileTargetPermanentAndTrackWithSourceEffect();
        target(TargetFilters.land())
                .bindEffect(exileTargetPermanentAndTrackWithSource);
        var exileTargetPermanentAndTrackWithSource2 = new ExileTargetPermanentAndTrackWithSourceEffect();
        target(TargetFilters.land())
                .bindEffect(exileTargetPermanentAndTrackWithSource2);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                exileTargetPermanentAndTrackWithSource,
                exileTargetPermanentAndTrackWithSource2);
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new ReturnAllCardsExiledWithSourceEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));
    }
}
