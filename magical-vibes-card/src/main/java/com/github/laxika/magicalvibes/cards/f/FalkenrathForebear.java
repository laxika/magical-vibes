package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "111")
public class FalkenrathForebear extends Card {

    public FalkenrathForebear() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, CreateTokenEffect.ofBloodToken(1));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2,
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD)),
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)),
                "{B}, Sacrifice two Blood tokens: Return this card from your graveyard to the battlefield."
        ));
    }
}
