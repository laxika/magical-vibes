package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "MSC", collectorNumber = "678")
public class AsgardianInspiration extends Card {

    public AsgardianInspiration() {
        addEffect(EffectSlot.SPELL, new ExileTopCardMayPlayThisTurnEffect(false));
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT,
                new MayPayManaEffect("{2}", new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {2} to return Asgardian Inspiration from your graveyard to your hand?"));
    }
}
