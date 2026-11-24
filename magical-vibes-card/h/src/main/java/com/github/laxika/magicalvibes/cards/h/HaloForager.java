package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsEventValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "227")
@CardRegistration(set = "MOM", collectorNumber = "385")
@CardRegistration(set = "WOC", collectorNumber = "139")
public class HaloForager extends Card {

    public HaloForager() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(SequenceEffect.of(
                        EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                        QueueReflexiveAbilityEffect.withEventValue(
                                new CastTargetInstantOrSorceryFromGraveyardEffect(
                                        GraveyardSearchScope.ALL_GRAVEYARDS, true, true,
                                        new CardAllOfPredicate(List.of(
                                                new CardAnyOfPredicate(List.of(
                                                        new CardTypePredicate(CardType.INSTANT),
                                                        new CardTypePredicate(CardType.SORCERY))),
                                                new CardManaValueEqualsEventValuePredicate()))),
                                AmountThisWay.manaPaid())),
                        "Pay {X} to cast an instant or sorcery card with mana value X from a graveyard?"));
    }
}
