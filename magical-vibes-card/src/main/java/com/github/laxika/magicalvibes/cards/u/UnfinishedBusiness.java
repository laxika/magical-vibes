package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureAndAurasOrEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "259")
@CardRegistration(set = "WOC", collectorNumber = "8")
@CardRegistration(set = "WOC", collectorNumber = "44")
public class UnfinishedBusiness extends Card {

    public UnfinishedBusiness() {
        ReturnTargetCreatureAndAurasOrEquipmentEffect effect =
                new ReturnTargetCreatureAndAurasOrEquipmentEffect();

        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.SPELL, effect);
        target(new GraveyardCardPredicateTargetFilter(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.AURA),
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT))),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 2)
                .addEffect(EffectSlot.SPELL, effect);
    }
}
