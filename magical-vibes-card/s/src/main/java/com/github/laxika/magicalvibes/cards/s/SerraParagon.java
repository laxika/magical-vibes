package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "32")
@CardRegistration(set = "SOC", collectorNumber = "169")
public class SerraParagon extends Card {

    public SerraParagon() {
        addStaticEffect(new PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
                new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardMaxManaValuePredicate(3))),
                new GrantTriggeredAbilityToCastSpellEffect(
                        new TriggeredAbility(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS),
                        SequenceEffect.of(
                                new ExileSourceCardFromGraveyardEffect(),
                                new GainLifeEffect(2))))));
    }
}
