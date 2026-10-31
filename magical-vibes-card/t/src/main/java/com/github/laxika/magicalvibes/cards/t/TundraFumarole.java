package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "KHM", collectorNumber = "156")
@CardRegistration(set = "KHM", collectorNumber = "361")
public class TundraFumarole extends Card {

    public TundraFumarole() {
        addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(4));
        addSpellEffect(new AwardPersistentManaEffect(
                ManaColor.COLORLESS, new SnowManaSpentToCast()));
    }
}
