package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "50")
public class Slithermuse extends Card {

    public Slithermuse() {
        // "When this creature leaves the battlefield, choose an opponent. If that player has more cards
        // in hand than you, draw cards equal to the difference." Two-player: the opponent is unique, so
        // draw (opponent hand - your hand). A non-positive difference draws 0 (applyDrawCards no-ops on
        // <= 0), matching the "if that player has more cards" intervening check. Non-targeting choice,
        // so it pushes straight to the stack from the leaves-the-battlefield slot.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new DrawCardEffect(new Sum(
                        new CardsInHand(CountScope.OPPONENTS),
                        new Scaled(new CardsInHand(CountScope.CONTROLLER), -1))));

        // Evoke {3}{U}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{U}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
