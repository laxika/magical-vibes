package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "93")
public class DespoilerOfSouls extends Card {

    public DespoilerOfSouls() {
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // {B}{B}, Exile two other creature cards from your graveyard: Return this card from your
        // graveyard to the battlefield.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(new ExileNCardsFromGraveyardCost(2, CardType.CREATURE),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()),
                "{B}{B}, Exile two other creature cards from your graveyard: Return Despoiler of Souls "
                        + "from your graveyard to the battlefield."));
    }
}
