package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "230")
public class RalCracklingWit extends Card {

    public RalCracklingWit() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new PutCountersOnSelfEffect(CounterType.LOYALTY))));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(createOtterToken()),
                "+1: Create a 1/1 blue and red Otter creature token with prowess."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DrawCardEffect(3), new DiscardEffect(2, DiscardRecipient.CONTROLLER)),
                "−3: Draw three cards, then discard two cards."
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new DrawCardEffect(3), new CreateEmblemEffect(
                        List.of(StormEffect.forInstantOrSorcery()),
                        "Instant and sorcery spells you cast have storm.")),
                "−10: Draw three cards. You get an emblem with \"Instant and sorcery spells you cast have storm.\""
        ));
    }

    private CreateTokenEffect createOtterToken() {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Otter",
                1,
                1,
                CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.OTTER),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(
                        EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        new SpellCastTriggerEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                List.of(new BoostSelfEffect(1, 1)))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
