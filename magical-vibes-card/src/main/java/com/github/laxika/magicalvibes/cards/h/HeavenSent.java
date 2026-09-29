package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromExileThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WHO", collectorNumber = "134")
public class HeavenSent extends Card {

    public HeavenSent() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, CreateTokenEffect.ofClueToken(1));

        AnOpponentLifeAtMost opponentAtZero = new AnOpponentLifeAtMost(0);
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                ConditionalEffect.unless(opponentAtZero, new DrawCardEffect(7)));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                ConditionalEffect.unless(new NotCondition(opponentAtZero),
                        SequenceEffect.of(
                                new ExileSelfEffect(),
                                new AllowCastSourceCardFromExileThisTurnEffect())));
    }
}
