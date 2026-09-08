package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "95")
public class BloodFountain extends Card {

    public BloodFountain() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofBloodToken(1));

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
