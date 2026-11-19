package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToRandomOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPG", collectorNumber = "70")
@CardRegistration(set = "AFC", collectorNumber = "32")
@CardRegistration(set = "MB2", collectorNumber = "195")
public class MaddeningHex extends Card {

    public MaddeningHex() {
        AttachSourceAuraToRandomOpponentEffect reattach = new AttachSourceAuraToRandomOpponentEffect();
        RollD6Effect roll = new RollD6Effect(List.of(
                SequenceEffect.of(new DealDamageToPlayersEffect(1, Subjects.triggeringPlayer()), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(2, Subjects.triggeringPlayer()), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(3, Subjects.triggeringPlayer()), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(4, Subjects.triggeringPlayer()), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(5, Subjects.triggeringPlayer()), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(6, Subjects.triggeringPlayer()), reattach)));

        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))
                        .withStackEntryFilter(new StackEntryControlledByEnchantedPlayerPredicate()),
                roll);
    }
}
