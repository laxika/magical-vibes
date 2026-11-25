package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "114")
public class ForgottenHarvest extends Card {

    public ForgottenHarvest() {
        target(TargetFilters.creature()).addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        ExileUpToOneCardFromGraveyardEffect.oneMatching(new CardTypePredicate(CardType.LAND))
                                .from(Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.exiled(),
                                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                "Exile a land card from your graveyard?"));
    }
}
