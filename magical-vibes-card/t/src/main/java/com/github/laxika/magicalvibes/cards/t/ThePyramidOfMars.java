package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "597")
public class ThePyramidOfMars extends Card {

    public ThePyramidOfMars() {
        addTrigger(Triggers.planeswalkToThis(), new SurveilEffect(2));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SurveilEffect(2));

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addTrigger(Triggers.chaosEnsues(), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(creature)
                .targetGraveyard(true)
                .build());
    }
}
