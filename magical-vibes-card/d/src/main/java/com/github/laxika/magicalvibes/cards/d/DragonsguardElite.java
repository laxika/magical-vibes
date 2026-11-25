package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "127")
public class DragonsguardElite extends Card {

    public DragonsguardElite() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> magecraft = List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), magecraft.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell().withFilter(instantOrSorcery), magecraft.toArray(CardEffect[]::new));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(new DoublePlusOneCountersOnTargetCreatureEffect(Subjects.source())),
                "{4}{G}{G}: Double the number of +1/+1 counters on this creature."));
    }
}
