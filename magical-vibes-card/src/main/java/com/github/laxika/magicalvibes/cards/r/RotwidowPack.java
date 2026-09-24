package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "212")
public class RotwidowPack extends Card {

    public RotwidowPack() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{G}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new CreateTokenEffect(
                                "Spider", 1, 2, CardColor.GREEN,
                                List.of(CardSubtype.SPIDER),
                                Set.of(Keyword.REACH),
                                Set.of()
                        ),
                        new LoseLifeEffect(
                                new PermanentCount(
                                        new PermanentHasSubtypePredicate(CardSubtype.SPIDER),
                                        CountScope.CONTROLLER),
                                LoseLifeRecipient.EACH_OPPONENT)
                ),
                "{3}{B}{G}, Exile a creature card from your graveyard: Create a 1/2 green Spider creature token with reach, then each opponent loses 1 life for each Spider you control."
        ));
    }
}
