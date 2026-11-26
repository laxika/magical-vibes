package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ZNR", collectorNumber = "249")
public class RelicGolem extends Card {

    public RelicGolem() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attackOrBlock(
                new OpponentGraveyardAtLeast(8),
                "an opponent has eight or more cards in their graveyard"
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new MillEffect(2, Subjects.targetPlayer())),
                "{2}, {T}: Target player mills two cards."
        ));
    }
}
