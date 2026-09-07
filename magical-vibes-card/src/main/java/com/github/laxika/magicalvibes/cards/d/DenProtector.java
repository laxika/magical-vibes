package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithLessPowerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "DTK", collectorNumber = "181")
public class DenProtector extends Card {

    public DenProtector() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithLessPowerEffect());
        addMorph("{1}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));

        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.ON_TURNED_FACE_UP, ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build());
    }
}
