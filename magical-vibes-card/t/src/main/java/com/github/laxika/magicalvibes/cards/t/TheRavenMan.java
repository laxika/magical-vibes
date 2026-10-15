package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "103")
@CardRegistration(set = "DMU", collectorNumber = "289")
public class TheRavenMan extends Card {

    public TheRavenMan() {
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(new AnyPlayerDiscardedCardThisTurn(), birdToken()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{B}",
                List.of(new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT)),
                "{3}{B}, {T}: Each opponent discards a card. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static CreateTokenEffect birdToken() {
        return new CreateTokenEffect(
                1,
                "Bird",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.BIRD),
                Set.of(Keyword.FLYING),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CantBlockEffect())
        );
    }
}
