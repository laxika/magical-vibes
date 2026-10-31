package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MKM", collectorNumber = "244")
@CardRegistration(set = "MKM", collectorNumber = "316")
@CardRegistration(set = "EOC", collectorNumber = "130")
public class WorldsoulsRage extends Card {

    public WorldsoulsRage() {
        addSpellEffect(new DealDamageToAnyTargetEffect(new XValue()));
        addSpellEffect(new PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect(
                new CardTypePredicate(CardType.LAND), "land", new XValue()));
    }
}
