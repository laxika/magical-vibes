package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "44")
@CardRegistration(set = "ACR", collectorNumber = "136")
public class AdWalBreakerOfChains extends Card {

    public AdWalBreakerOfChains() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LookAtTopCardsEffect(
                new Fixed(6),
                new Fixed(1),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ASSASSIN),
                        new CardSubtypePredicate(CardSubtype.PIRATE),
                        new CardSubtypePredicate(CardSubtype.VEHICLE))),
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                true));

        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE),
                        new MayEffect(
                                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "return this card from your graveyard to your hand")));
    }
}
