package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "163")
public class ZukoExiledPrince extends Card {

    public ZukoExiledPrince() {
        addEffect(EffectSlot.ON_ATTACK, new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 3));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ExileTopCardMayPlayThisTurnEffect(false)),
                "{3}: Exile the top card of your library. You may play that card this turn."
        ));
    }
}
