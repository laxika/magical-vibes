package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "124")
public class MakeshiftMannequin extends Card {

    public MakeshiftMannequin() {
        // Return target creature card from your graveyard to the battlefield with a mannequin
        // counter on it. For as long as that creature has a mannequin counter on it, it has
        // "When this creature becomes the target of a spell or ability, sacrifice it."
        // The mannequin counter (added on entry) drives the granted ability in
        // TriggerCollectionService.
        addEffect(EffectSlot.SPELL,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .enterWithMannequinCounter(true)
                        .build());
    }
}
