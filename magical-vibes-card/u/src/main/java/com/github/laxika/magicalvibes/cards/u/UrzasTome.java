package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "235")
public class UrzasTome extends Card {

    public UrzasTome() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new DrawCardEffect(1),
                        UnlessPaysEffect.unlessYouPay(
                                new ExileNCardsFromGraveyardCost(1, null, new CardIsHistoricPredicate()),
                                new DiscardEffect(1, Subjects.you(), false))
                ),
                "{3}, {T}: Draw a card. Then discard a card unless you exile a historic card from your graveyard."
        ));
    }
}
