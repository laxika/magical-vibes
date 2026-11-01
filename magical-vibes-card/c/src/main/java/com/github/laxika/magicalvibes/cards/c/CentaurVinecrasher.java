package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C15", collectorNumber = "35")
@CardRegistration(set = "C18", collectorNumber = "135")
@CardRegistration(set = "EOC", collectorNumber = "94")
@CardRegistration(set = "CMA", collectorNumber = "95")
public class CentaurVinecrasher extends Card {

    public CentaurVinecrasher() {
        addStaticEffect(new EnterWithCountersEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.ANY_PLAYER)));

        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.ANY),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayPayManaEffect(
                        "{G}{G}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {G}{G} to return Centaur Vinecrasher from your graveyard to your hand?"));
    }
}
