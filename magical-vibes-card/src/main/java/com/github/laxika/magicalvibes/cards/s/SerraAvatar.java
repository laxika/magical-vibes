package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "M13", collectorNumber = "32")
public class SerraAvatar extends Card {

    public SerraAvatar() {
        // Serra Avatar's power and toughness are each equal to your life total.
        ControllerLifeTotal life = new ControllerLifeTotal();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(life, life));
        // When Serra Avatar is put into a graveyard from anywhere, shuffle it into its owner's library.
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
