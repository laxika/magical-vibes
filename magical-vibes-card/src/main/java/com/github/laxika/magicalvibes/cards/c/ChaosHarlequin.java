package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "69")
public class ChaosHarlequin extends Card {

    public ChaosHarlequin() {
        // {R}: Exile the top card of your library. If that card is a land card, this creature
        // gets -4/-0 until end of turn. Otherwise, this creature gets +2/+0 until end of turn.
        // The exile imprints what it exiled so both branches can inspect it at resolution.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new ExileTopCardOfLibraryCost(1, true),
                        new ConditionalEffect(
                                new ImprintedCardMatches(new CardTypePredicate(CardType.LAND), "a land card"),
                                new BoostSelfEffect(-4, 0)
                        ),
                        new ConditionalEffect(
                                new ImprintedCardMatches(
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                        "not a land card"
                                ),
                                new BoostSelfEffect(2, 0)
                        )
                ),
                "{R}: Exile the top card of your library. If that card is a land card, this "
                        + "creature gets -4/-0 until end of turn. Otherwise, this creature gets "
                        + "+2/+0 until end of turn."
        ));
    }
}
