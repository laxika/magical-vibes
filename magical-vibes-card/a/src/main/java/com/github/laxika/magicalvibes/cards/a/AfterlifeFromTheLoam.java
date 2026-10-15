package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "TDC", collectorNumber = "25")
@CardRegistration(set = "TDC", collectorNumber = "65")
public class AfterlifeFromTheLoam extends Card {

    public AfterlifeFromTheLoam() {
        CardTypePredicate creatureCards = new CardTypePredicate(CardType.CREATURE);

        addEffect(EffectSlot.SPELL, new DelveCost());
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(new GraveyardCardPredicateTargetFilter(creatureCards, GraveyardSearchScope.ALL_GRAVEYARDS), 0, 99)
                .addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        creatureCards, 99, false, false, null, 0, null, CardSubtype.ZOMBIE, null,
                        0, GraveyardSearchScope.ALL_GRAVEYARDS, false, false, false));
    }
}
