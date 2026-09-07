package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "PLC", collectorNumber = "158")
public class IntetTheDreamer extends Card {

    public IntetTheDreamer() {
        // Whenever Intet deals combat damage to a player, you may pay {2}{U}. If you do, exile the
        // top card of your library face down.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayPayManaEffect("{2}{U}", new ExileTopCardsToSourceEffect(1), "Pay {2}{U}?"));

        // You may play that card without paying its mana cost for as long as Intet remains on the
        // battlefield.
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, false, 0, null, false, false, true));
    }
}
