package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "92")
public class KaervekThePunisher extends Card {

    public KaervekThePunisher() {
        CardPredicate black = new CardColorPredicate(CardColor.BLACK);
        target(new GraveyardCardPredicateTargetFilter(black, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1);
        addTrigger(Triggers.youCommitCrime(),
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        black,
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        2,
                        false,
                        false));
    }
}
