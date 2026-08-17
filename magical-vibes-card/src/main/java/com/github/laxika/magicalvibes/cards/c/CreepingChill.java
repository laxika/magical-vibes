package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "GRN", collectorNumber = "66")
public class CreepingChill extends Card {

    public CreepingChill() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
        addEffect(EffectSlot.ON_SELF_MILLED, new ConditionalEffect(
                new SourceCardInGraveyard(),
                new MayEffect(
                        SequenceEffect.of(
                                new ExileSourceCardFromGraveyardEffect(),
                                new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT),
                                new GainLifeEffect(3)),
                        "Exile Creeping Chill and deal 3 damage to each opponent?")));
    }
}
