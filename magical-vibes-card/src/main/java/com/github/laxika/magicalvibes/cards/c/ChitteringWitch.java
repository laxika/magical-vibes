package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PlayersWithCardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "175")
@CardRegistration(set = "AFC", collectorNumber = "95")
@CardRegistration(set = "BLC", collectorNumber = "180")
public class ChitteringWitch extends Card {

    public ChitteringWitch() {
        // When this creature enters, create a number of 1/1 black Rat creature tokens equal to the
        // number of opponents you have.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new PlayersWithCardsInHandAtLeast(CountScope.OPPONENTS, 0),
                "Rat", 1, 1, CardColor.BLACK, List.of(CardSubtype.RAT), Set.of(), Set.of()));

        // {1}{B}, Sacrifice a creature: Target creature gets -2/-2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeCreatureCost(), new BoostTargetCreatureEffect(-2, -2)),
                "{1}{B}, Sacrifice a creature: Target creature gets -2/-2 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
