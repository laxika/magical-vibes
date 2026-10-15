package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "197")
public class SayItsName extends Card {

    public SayItsName() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new MayEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardTypePredicate(CardType.LAND))))
                                .build(),
                        "Return a creature or land card from your graveyard to your hand?")));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new ExileNCardsFromGraveyardCost(2, null, new CardNamedPredicate("Say Its Name")),
                        new SearchZonesForCardNamedToBattlefieldEffect("Altanak, the Thrice-Called")),
                "Exile this card and two other cards named Say Its Name from your graveyard: Search your "
                        + "graveyard, hand, and/or library for a card named Altanak, the Thrice-Called and "
                        + "put it onto the battlefield. If you search your library this way, shuffle. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
