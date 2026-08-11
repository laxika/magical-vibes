package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.OpponentPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "77")
public class WhiplashTrap extends Card {

    public WhiplashTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{U}")),
                new OpponentPermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE), 2),
                false));

        target(TargetFilters.creature(), 2, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
