package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "144")
public class TavernScoundrel extends Card {

    public TavernScoundrel() {
        addEffect(EffectSlot.ON_CONTROLLER_WINS_COIN_FLIP, CreateTokenEffect.ofTreasureToken(2));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificePermanentCost(new PermanentTruePredicate(), "Sacrifice another permanent"),
                        new FlipCoinWinEffect(null)
                ),
                "{1}, {T}, Sacrifice another permanent: Flip a coin."
        ));
    }
}
