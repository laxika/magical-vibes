package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostAmountPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "12")
public class GhituEmbercoiler extends Card {

    public GhituEmbercoiler() {
        // At the beginning of your first main phase, you may discard a card. If you do, seek a
        // card with greater mana value and exile it. Until the end of your next turn, you may play
        // the exiled card.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                SeekLibraryEffect.exiledPlayableUntilNextTurn(1, new CardNotPredicate(
                                        new CardManaValueAtMostAmountPredicate(new LastDiscardedCardManaValue()))))),
                "Discard a card to seek a card with greater mana value?"));
    }
}
