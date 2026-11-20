package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

/** Back face of Soundwave, Sonic Spy. */
public class SoundwaveSuperiorCaptain extends Card {

    public SoundwaveSuperiorCaptain() {
        CreateTokenEffect ravage = new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Ravage", 3, 3, CardColor.BLACK,
                Set.of(CardColor.BLACK), List.of(CardSubtype.ROBOT),
                Set.of(Keyword.MENACE, Keyword.DEATHTOUCH), Set.of(CardType.CREATURE),
                false, false, List.of(), List.of(), false, false, true, 0, Set.of());
        CreateTokenEffect laserbeak = new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Laserbeak", 2, 2, CardColor.BLUE,
                Set.of(CardColor.BLUE), List.of(CardSubtype.ROBOT),
                Set.of(Keyword.FLYING, Keyword.HEXPROOF), Set.of(CardType.CREATURE),
                false, false, List.of(), List.of(), false, false, true, 0, Set.of());

        addTrigger(Triggers.youCastSpell().withFilter(new CardManaValueParityPredicate(ManaValueParity.ODD)),
                SequenceEffect.of(new TransformSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.transformed(), ravage)));
        addTrigger(Triggers.youCastSpell().withFilter(new CardManaValueParityPredicate(ManaValueParity.EVEN)),
                SequenceEffect.of(new TransformSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.transformed(), laserbeak)));
    }
}
