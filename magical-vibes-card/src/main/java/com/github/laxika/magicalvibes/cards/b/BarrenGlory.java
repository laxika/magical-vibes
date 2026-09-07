package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "3")
public class BarrenGlory extends Card {

    public BarrenGlory() {
        // At the beginning of your upkeep, if you control no permanents other than this enchantment
        // and have no cards in hand, you win the game.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceIsOnBattlefield(),
                        new NoOtherPermanent(new PermanentTruePredicate()),
                        new ControllerHandEmpty())),
                new WinGameEffect()));
    }
}
