package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "231")
public class SoulOfNewPhyrexia extends Card {

    public SoulOfNewPhyrexia() {
        // {5}: Permanents you control gain indestructible until end of turn.
        // OWN_PERMANENTS walks the controller's whole battlefield, so the source is covered too.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS)),
                "{5}: Permanents you control gain indestructible until end of turn."
        ));

        // {5}, Exile this card from your graveyard: Permanents you control gain indestructible until end of turn.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS)
                ),
                "{5}, Exile this card from your graveyard: Permanents you control gain indestructible until end of turn."
        ));
    }
}
