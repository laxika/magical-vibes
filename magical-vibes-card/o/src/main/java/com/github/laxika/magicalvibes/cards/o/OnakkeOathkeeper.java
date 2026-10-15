package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "722")
@CardRegistration(set = "CMM", collectorNumber = "755")
public class OnakkeOathkeeper extends Card {

    public OnakkeOathkeeper() {
        // Creatures attacking a planeswalker you control cost its controller {1} per creature.
        addEffect(EffectSlot.STATIC, RequirePaymentToAttackEffect.planeswalkersOnly(1));

        // {4}{W}{W}, Exile this card from your graveyard: Return target planeswalker card
        // from your graveyard to the battlefield.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.PLANESWALKER))
                                .targetGraveyard(true)
                                .build()),
                "{4}{W}{W}, Exile this card from your graveyard: Return target planeswalker card from your graveyard to the battlefield."
        ));
    }
}
