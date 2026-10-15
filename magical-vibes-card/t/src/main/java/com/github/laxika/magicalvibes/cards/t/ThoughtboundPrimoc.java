package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.APlayerControlsMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "240")
public class ThoughtboundPrimoc extends Card {

    public ThoughtboundPrimoc() {
        PermanentPredicate wizards = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new APlayerControlsMoreCreaturesThanEachOtherPlayer(wizards),
                new PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect(wizards)));
    }
}
