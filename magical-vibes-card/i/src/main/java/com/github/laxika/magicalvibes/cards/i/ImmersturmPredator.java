package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "214")
@CardRegistration(set = "KHM", collectorNumber = "367")
public class ImmersturmPredator extends Card {

    public ImmersturmPredator() {
        addTrigger(Triggers.becomesTapped(),
                SequenceEffect.of(
                        ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard(),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice another creature"),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source()),
                        new TapPermanentsEffect(Subjects.source())),
                "Sacrifice another creature: This creature gains indestructible until end of turn. Tap it."));
    }
}
