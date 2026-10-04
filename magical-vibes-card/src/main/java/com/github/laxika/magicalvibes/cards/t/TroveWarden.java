package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "ZNC", collectorNumber = "3")
public class TroveWarden extends Card {

    public TroveWarden() {
        var permanentWithManaValueAtMostThree = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardMaxManaValuePredicate(3)));

        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new ExileTargetCardFromGraveyardAndTrackWithSourceEffect(
                        permanentWithManaValueAtMostThree,
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        addEffect(EffectSlot.ON_DEATH, new ReturnAllCardsExiledWithSourceEffect());
    }
}
