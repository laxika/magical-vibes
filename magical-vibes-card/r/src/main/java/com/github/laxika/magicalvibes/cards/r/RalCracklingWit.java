package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BLB", collectorNumber = "230")
public class RalCracklingWit extends Card {

    public RalCracklingWit() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                PutCountersEffect.onSelf(CounterType.LOYALTY));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(createOtterToken()),
                "+1: Create a 1/1 blue and red Otter creature token with prowess."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DrawCardEffect(3), new DiscardEffect(2, Subjects.you())),
                "−3: Draw three cards, then discard two cards."
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new DrawCardEffect(3), new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))),
                                CopyTriggeringSpellEffect.storm()),
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
                List.of(
                        new TriggeredAbility(Triggers.youCastSpell()
                                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                                BoostEffect.self(1, 1))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
