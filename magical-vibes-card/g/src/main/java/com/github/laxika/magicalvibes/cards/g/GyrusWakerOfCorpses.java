package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C18", collectorNumber = "41")
public class GyrusWakerOfCorpses extends Card {

    public GyrusWakerOfCorpses() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new ManaSpentToCast()));

        var lesserPowerCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardPowerLessThanSourcePowerPredicate()));
        addTrigger(Triggers.attacks(),
                new MayEffect(
                        ExileTargetCardFromGraveyardAndCreateTokenCopyEffect
                                .tappedAndAttackingExiledAtEndOfCombat(
                                        lesserPowerCreature, true, List.of()),
                        "Exile target creature card with lesser power from your graveyard?"));
    }
}
