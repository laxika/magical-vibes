package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "NEO", collectorNumber = "228")
public class KotoseTheSilentSpider extends Card {

    public KotoseTheSilentSpider() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetGraveyardCardAndSameNameFromZonesEffect(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD,
                        new CardNotPredicate(CardPredicateUtils.basicLand()), true));
        addEffect(EffectSlot.STATIC, AllowCastFromCardsExiledWithSourceEffect.oneCardWithAnyMana());
    }
}
