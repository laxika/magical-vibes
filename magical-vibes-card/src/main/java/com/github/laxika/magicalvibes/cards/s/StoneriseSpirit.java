package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "32")
public class StoneriseSpirit extends Card {

    public StoneriseSpirit() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new ExileCardFromGraveyardCost((CardType) null),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)
                ),
                "{4}, Exile a card from your graveyard: Target creature gains flying until end of turn.",
                TargetFilters.creature()
        ));
    }
}
