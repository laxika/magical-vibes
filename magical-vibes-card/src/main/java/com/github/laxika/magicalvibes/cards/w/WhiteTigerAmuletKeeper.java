package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "736")
public class WhiteTigerAmuletKeeper extends Card {

    public WhiteTigerAmuletKeeper() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new DrawCardEffect(1),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                                "Put a land card from your hand onto the battlefield?")),
                "{3}{G}, Exile this card from your graveyard: Draw a card. You may put a land card from your hand "
                        + "onto the battlefield."
        ));
    }
}
