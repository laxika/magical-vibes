package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNC", collectorNumber = "3")
public class TroveWarden extends Card {

    public TroveWarden() {
        var permanentWithManaValueAtMostThree = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardMaxManaValuePredicate(3)));

        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new ExileTargetCardFromGraveyardAndTrackWithSourceEffect(
                        permanentWithManaValueAtMostThree,
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnAllCardsExiledWithSourceEffect());
    }
}
