package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDragonApproachAndSearchEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "STX", collectorNumber = "97")
@CardRegistration(set = "MB2", collectorNumber = "246")
public class DragonsApproach extends Card {

    public DragonsApproach() {
        addSpellEffect(new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT));
        addSpellEffect(new MayEffect(
                new ExileDragonApproachAndSearchEffect(),
                "Exile Dragon's Approach and four cards named Dragon's Approach from your graveyard?"));
    }
}
