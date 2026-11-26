package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "190")
@CardRegistration(set = "GK1", collectorNumber = "83")
public class AgrusKosWojekVeteran extends Card {

    public AgrusKosWojekVeteran() {
        addTrigger(Triggers.attacks(), BoostEffect.allCreatures(2, 0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.RED))))),
                BoostEffect.allCreatures(0, 2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))))));
    }
}
