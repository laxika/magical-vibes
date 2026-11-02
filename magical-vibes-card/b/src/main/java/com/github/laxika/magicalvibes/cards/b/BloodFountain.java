package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "95")
@CardRegistration(set = "DBL", collectorNumber = "362")
public class BloodFountain extends Card {

    public BloodFountain() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofBloodToken(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardTypePredicate(CardType.CREATURE), 2)),
                "{3}{B}, {T}, Sacrifice this artifact: Return up to two target creature cards "
                        + "from your graveyard to your hand."
        ));
    }
}
