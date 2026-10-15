package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "249")
public class RelicGolem extends Card {

    public RelicGolem() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new OpponentGraveyardAtLeast(8),
                "an opponent has eight or more cards in their graveyard"
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new MillEffect(2, MillRecipient.TARGET_PLAYER)),
                "{2}, {T}: Target player mills two cards."
        ));
    }
}
