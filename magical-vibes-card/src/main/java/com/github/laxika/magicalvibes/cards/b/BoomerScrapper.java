package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "95")
@CardRegistration(set = "PIP", collectorNumber = "408")
@CardRegistration(set = "PIP", collectorNumber = "623")
@CardRegistration(set = "PIP", collectorNumber = "936")
public class BoomerScrapper extends Card {

    public BoomerScrapper() {
        SequenceEffect loseLifeAndCreateJunk = SequenceEffect.of(
                new LoseLifeEffect(1), junkToken());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, loseLifeAndCreateJunk);
        addEffect(EffectSlot.ON_ATTACK, loseLifeAndCreateJunk);
        addEffect(EffectSlot.ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsTokenPredicate(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }

    private static CreateTokenEffect junkToken() {
        return CreateTokenEffect.ofArtifactToken(
                1,
                "Junk",
                List.of(CardSubtype.JUNK),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSelfCost(), new ExileTopCardMayPlayThisTurnEffect(false)),
                        "{T}, Sacrifice this token: Exile the top card of your library. You may play that card this turn. "
                                + "Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED)));
    }
}
