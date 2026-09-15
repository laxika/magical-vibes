package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "39")
public class ZhalfirinDecoy extends Card {

    public ZhalfirinDecoy() {
        // {T}: Tap target creature. Activate only if you had a creature enter the battlefield under your control this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Tap target creature. Activate only if you had a creature enter the battlefield under your control this turn.",
                TargetFilters.creature()
        ).withActivationCondition(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE), 1),
                "Activate only if you had a creature enter the battlefield under your control this turn."));
    }
}
