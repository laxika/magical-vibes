package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "23")
public class FurgulQuagNurturer extends Card {

    public FurgulQuagNurturer() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalReplacementEffect(
                        new ControlsPermanentCountAtMost(0,
                                new PermanentHasSubtypePredicate(CardSubtype.LEECH)),
                        PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentHasSubtypePredicate(CardSubtype.LEECH)),
                        ConjureCardsEffect.named("MKM", "104", ConjureDestination.ontoBattlefield())));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeCreatureCost(false, true),
                        new AwardManaEffect(ManaColor.GREEN, new XValue())),
                "{T}, Sacrifice a creature: Add an amount of {G} equal to the sacrificed creature's power."
        ));
    }
}
