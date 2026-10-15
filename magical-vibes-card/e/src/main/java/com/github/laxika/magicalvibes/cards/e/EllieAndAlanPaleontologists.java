package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.ImprintedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "REX", collectorNumber = "10")
@CardRegistration(set = "REX", collectorNumber = "35")
public class EllieAndAlanPaleontologists extends Card {

    public EllieAndAlanPaleontologists() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE, false, true),
                        new DiscoverEffect(new ImprintedCardManaValue())
                ),
                "{T}, Exile a creature card from your graveyard: Discover X, where X is the mana value of the exiled card. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
