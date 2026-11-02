package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "111")
@CardRegistration(set = "DBL", collectorNumber = "378")
public class FalkenrathForebear extends Card {

    public FalkenrathForebear() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofBloodToken(1));

        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                        new PermanentIsTokenPredicate()))),
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)),
                "{B}, Sacrifice two Blood tokens: Return this card from your graveyard to the battlefield."
        ));
    }
}
