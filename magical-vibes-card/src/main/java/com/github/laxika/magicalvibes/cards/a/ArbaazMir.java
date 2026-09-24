package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "46")
@CardRegistration(set = "ACR", collectorNumber = "138")
public class ArbaazMir extends Card {

    public ArbaazMir() {
        // Whenever Arbaaz Mir or another nontoken historic permanent you control enters,
        // Arbaaz Mir deals 1 damage to each opponent and you gain 1 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new NotCondition(new SourceIsToken()), triggerEffect()));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsHistoricPredicate(),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        triggerEffect()));
    }

    private static SequenceEffect triggerEffect() {
        return SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(1));
    }
}
