package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndPerpetuallyModifySoughtCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "56")
public class GoblinTrapfinder extends Card {

    public GoblinTrapfinder() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new SeekLibraryAndPerpetuallyModifySoughtCardEffect(
                        new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.CREATURE))),
                        3,
                        Set.of(Keyword.HASTE),
                        1,
                        EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new SacrificeSelfEffect()));
    }
}
