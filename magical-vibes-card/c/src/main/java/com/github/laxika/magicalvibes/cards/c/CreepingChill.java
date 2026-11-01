package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "66")
public class CreepingChill extends Card {

    public CreepingChill() {
        addSpellEffect(new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT));
        addSpellEffect(new GainLifeEffect(3));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, ObjectRelation.THIS), new ConditionalEffect(
                new SourceCardInGraveyard(),
                new MayEffect(
                        SequenceEffect.of(
                                new ExileSourceCardFromGraveyardEffect(),
                                new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT),
                                new GainLifeEffect(3)),
                        "Exile Creeping Chill and deal 3 damage to each opponent?")));
    }
}
