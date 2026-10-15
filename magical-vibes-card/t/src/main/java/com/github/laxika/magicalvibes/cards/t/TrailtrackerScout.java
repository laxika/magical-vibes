package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "35")
@CardRegistration(set = "BLC", collectorNumber = "68")
public class TrailtrackerScout extends Card {

    public TrailtrackerScout() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.wheneverYouExpend(
                8,
                List.of(ReturnTargetCardsFromGraveyardToHandEffect.forTriggeredAbility(
                        new CardIsPermanentPredicate(), 1))));
    }
}
