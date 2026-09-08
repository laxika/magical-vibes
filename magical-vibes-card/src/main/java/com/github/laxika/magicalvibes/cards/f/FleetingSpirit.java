package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "14")
public class FleetingSpirit extends Card {

    public FleetingSpirit() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(
                        new ExileNCardsFromGraveyardCost(3, null),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)
                ),
                "{W}, Exile three cards from your graveyard: This creature gains first strike until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false)
                ),
                "Discard a card: Exile this creature. Return it to the battlefield under its owner's control at the beginning of the next end step."
        ));
    }
}
