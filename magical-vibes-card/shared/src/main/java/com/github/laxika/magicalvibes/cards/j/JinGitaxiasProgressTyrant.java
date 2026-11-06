package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "NEO", collectorNumber = "59")
public class JinGitaxiasProgressTyrant extends Card {

    public JinGitaxiasProgressTyrant() {
        CardAnyOfPredicate artifactInstantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ));

        addTrigger(Triggers.youCastSpell().withFilter(artifactInstantOrSorcery),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CopyTriggeringSpellEffect());
        addTrigger(Triggers.opponentCastsSpell().withFilter(artifactInstantOrSorcery),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CounterSpellEffect());
    }
}
