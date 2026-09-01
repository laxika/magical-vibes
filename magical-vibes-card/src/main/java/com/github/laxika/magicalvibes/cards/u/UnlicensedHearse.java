package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "246")
public class UnlicensedHearse extends Card {

    public UnlicensedHearse() {
        CardsExiledWithSource exiledCards = new CardsExiledWithSource();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(exiledCards, exiledCards));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileGraveyardCardsEffect(
                        2, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD, null, null, false, true, false)),
                "{T}: Exile up to two target cards from a single graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
