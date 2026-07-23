package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "127")
public class GangrenousZombies extends Card {

    public GangrenousZombies() {
        // {T}, Sacrifice this creature: This creature deals 1 damage to each creature and each player.
        // If you control a snow Swamp, this creature deals 2 damage to each creature and each player instead.
        var snowSwamp = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                new PermanentHasSupertypePredicate(CardSupertype.SNOW)
        ));
        var controlsSnowSwamp = new ControlsPermanent(snowSwamp);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new ConditionalEffect(new NotCondition(controlsSnowSwamp), new MassDamageEffect(1, true)),
                        new ConditionalEffect(controlsSnowSwamp, new MassDamageEffect(2, true))
                ),
                "{T}, Sacrifice this creature: This creature deals 1 damage to each creature and each player. "
                        + "If you control a snow Swamp, this creature deals 2 damage to each creature and each player instead."
        ));
    }
}
