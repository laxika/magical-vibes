package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "44")
@CardRegistration(set = "MAT", collectorNumber = "94")
@CardRegistration(set = "MAT", collectorNumber = "144")
@CardRegistration(set = "MAT", collectorNumber = "179")
@CardRegistration(set = "MAT", collectorNumber = "223")
public class RoccoStreetChef extends Card {

    public RoccoStreetChef() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new EachPlayerExilesTopCardMayPlayUntilNextEndStepEffect());

        CardEffect reward = SequenceEffect.of(
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()),
                foodToken());

        target(TargetFilters.creature()).addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.EXILE)),
                reward);

        addTrigger(Triggers.youPlayLand().playedFrom(Zone.EXILE), reward);
        addTrigger(Triggers.opponentPlaysLand().playedFrom(Zone.EXILE), reward);
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }
}
