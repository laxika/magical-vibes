package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "134")
@CardRegistration(set = "WHO", collectorNumber = "739")
public class HeavenSent extends Card {

    public HeavenSent() {
        addTrigger(Triggers.chapter(1), CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.chapter(2), CreateTokenEffect.ofClueToken(1));

        AnOpponentLifeAtMost opponentAtZero = new AnOpponentLifeAtMost(0);
        addTrigger(Triggers.chapter(3),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                new ConditionalEffect(opponentAtZero, new DrawCardEffect(7)),
                new ConditionalEffect(new NotCondition(opponentAtZero),
                        SequenceEffect.of(
                                new ExileSelfEffect(),
                                AllowCastSourceCardFromExileEffect.thisTurn())));
    }
}
