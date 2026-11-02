package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "228")
public class KotoseTheSilentSpider extends Card {

    public KotoseTheSilentSpider() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetGraveyardCardAndSameNameFromZonesEffect(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD,
                        new CardNotPredicate(CardPredicateUtils.basicLand()), true));
        addStaticEffect(AllowCastFromCardsExiledWithSourceEffect.oneCardWithAnyMana());
    }
}
