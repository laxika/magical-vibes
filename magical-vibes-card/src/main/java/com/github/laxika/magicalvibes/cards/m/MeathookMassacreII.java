package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DSK", collectorNumber = "108")
public class MeathookMassacreII extends Card {

    public MeathookMassacreII() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsEffect(new XValue(), new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.EACH_PLAYER).withSimultaneousChoices());

        ReturnDyingCreatureToBattlefieldEffect returnCreature = new ReturnDyingCreatureToBattlefieldEffect(
                false, CounterType.FINALITY, 1);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new MayPayLifeEffect(3, returnCreature,
                        "Pay 3 life to return that card to the battlefield under your control?"));

        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new MayPayLifeEffect(3, null,
                        "Pay 3 life to keep that card in its graveyard?",
                        MayPayPayer.TRIGGERING_PLAYER, returnCreature));
    }
}
