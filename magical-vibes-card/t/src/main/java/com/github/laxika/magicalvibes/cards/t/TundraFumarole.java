package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "KHM", collectorNumber = "156")
@CardRegistration(set = "KHM", collectorNumber = "361")
public class TundraFumarole extends Card {

    public TundraFumarole() {
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(4));
        addSpellEffect(new AwardPersistentManaEffect(
                ManaColor.COLORLESS, new SnowManaSpentToCast()));
    }
}
