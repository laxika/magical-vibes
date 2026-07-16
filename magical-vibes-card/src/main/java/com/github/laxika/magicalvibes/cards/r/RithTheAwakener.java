package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerPermanentOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DRB", collectorNumber = "12")
public class RithTheAwakener extends Card {

    public RithTheAwakener() {
        // Flying is auto-loaded from Scryfall.
        // Whenever Rith deals combat damage to a player, you may pay {2}{G}. If you do, choose a
        // color, then create a 1/1 green Saproling creature token for each permanent of that color.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayPayManaEffect("{2}{G}",
                        new CreateTokenPerPermanentOfChosenColorEffect(new CreateTokenEffect(
                                "Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                        "Pay {2}{G}?"));
    }
}
