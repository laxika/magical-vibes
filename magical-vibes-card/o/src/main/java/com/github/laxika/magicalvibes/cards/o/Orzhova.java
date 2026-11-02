package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "30")
@CardRegistration(set = "OPCA", collectorNumber = "60")
public class Orzhova extends Card {

    public Orzhova() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);

        addTrigger(Triggers.planeswalkAwayFromThis(),
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(Integer.MAX_VALUE, creature));

        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE);
        target(new GraveyardCardPredicateTargetFilter(creature, GraveyardSearchScope.OPPONENT_GRAVEYARD), 0, 99)
                .addTrigger(Triggers.chaosEnsues(),
                        new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD,
                                creature));
    }
}
