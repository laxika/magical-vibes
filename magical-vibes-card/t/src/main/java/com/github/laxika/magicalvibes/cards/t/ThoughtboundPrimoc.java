package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.APlayerControlsMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "240")
public class ThoughtboundPrimoc extends Card {

    public ThoughtboundPrimoc() {
        PermanentPredicate wizards = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new APlayerControlsMoreCreaturesThanEachOtherPlayer(wizards),
                new PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect(wizards)));
    }
}
