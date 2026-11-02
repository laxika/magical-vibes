package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "597")
public class ThePyramidOfMars extends Card {

    public ThePyramidOfMars() {
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, new SurveilEffect(2));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SurveilEffect(2));

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.CHAOS_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(creature)
                .targetGraveyard(true)
                .build());
    }
}
