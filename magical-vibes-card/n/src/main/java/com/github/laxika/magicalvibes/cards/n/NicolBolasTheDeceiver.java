package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TormentOfHailfireEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "205")
public class NicolBolasTheDeceiver extends Card {

    public NicolBolasTheDeceiver() {
        // +3: Each opponent loses 3 life unless that player sacrifices a nonland permanent of their
        // choice or discards a card. One fixed pass of Torment of Hailfire's per-opponent punisher.
        addActivatedAbility(new ActivatedAbility(
                +3,
                List.of(TormentOfHailfireEffect.once(3)),
                "+3: Each opponent loses 3 life unless that player sacrifices a nonland permanent "
                        + "of their choice or discards a card."
        ));

        // −3: Destroy target creature. Draw a card.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect(), new DrawCardEffect(1)),
                "−3: Destroy target creature. Draw a card.",
                TargetFilters.creature()
        ));

        // −11: Nicol Bolas deals 7 damage to each opponent. You draw seven cards.
        addActivatedAbility(new ActivatedAbility(
                -11,
                List.of(
                        new DealDamageToPlayersEffect(7, DamageRecipient.EACH_OPPONENT),
                        new DrawCardEffect(7)
                ),
                "−11: Nicol Bolas deals 7 damage to each opponent. You draw seven cards."
        ));
    }
}
