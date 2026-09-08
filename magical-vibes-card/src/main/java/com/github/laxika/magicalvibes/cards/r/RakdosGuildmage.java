package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "145")
public class RakdosGuildmage extends Card {

    public RakdosGuildmage() {
        // {3}{B}, Discard a card: Target creature gets -2/-2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostTargetCreatureEffect(-2, -2)
                ),
                "{3}{B}, Discard a card: Target creature gets -2/-2 until end of turn.",
                TargetFilters.creature()
        ));

        // {3}{R}: Create a 2/1 red Goblin creature token with haste. Exile it at the beginning of
        // the next end step.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new CreateTokenEffect(
                        CardType.CREATURE,
                        1,
                        "Goblin",
                        2,
                        1,
                        CardColor.RED,
                        null,
                        List.of(CardSubtype.GOBLIN),
                        Set.of(Keyword.HASTE),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(),
                        false,
                        true,
                        false,
                        0,
                        Set.of()
                )),
                "{3}{R}: Create a 2/1 red Goblin creature token with haste. Exile it at the beginning of the next end step."
        ));
    }
}
