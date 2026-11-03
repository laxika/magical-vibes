package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAttachedPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "59")
public class KassandraEagleBearer extends Card {

    public KassandraEagleBearer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SearchZonesForCardNamedToBattlefieldEffect.alwaysShuffling("The Spear of Leonidas"));

        PermanentAllOfPredicate legendaryEquipment = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
        PermanentAllOfPredicate qualifyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasAttachedPermanentPredicate(legendaryEquipment)));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(qualifyingCreature),
                new DrawCardEffect(1));
    }
}
