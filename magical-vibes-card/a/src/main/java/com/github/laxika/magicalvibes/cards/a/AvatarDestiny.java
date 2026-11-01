package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutMilledCreaturesOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "165")
public class AvatarDestiny extends Card {

    public AvatarDestiny() {
        CardsInGraveyard creatureCards = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);

        target(TargetFilters.creatureYouControl())
                .addStaticEffect(new AttachedBoostEffect(
                        creatureCards, creatureCards, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.AVATAR, GrantScope.ENCHANTED_CREATURE));

        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(new EventValue(), 1, true));
    }
}
