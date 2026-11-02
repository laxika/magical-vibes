package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "59")
public class JinGitaxiasProgressTyrant extends Card {

    public JinGitaxiasProgressTyrant() {
        CardAnyOfPredicate artifactInstantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ));

        addTrigger(Triggers.youCastSpell(), OncePerTurnTriggerEffect.keyed(
                new CopyControllerCastSpellOnSpellCastEffect(artifactInstantOrSorcery, null, null), "copy"));
        addTrigger(Triggers.opponentCastsSpell(), OncePerTurnTriggerEffect.keyed(
                new SpellCastTriggerEffect(artifactInstantOrSorcery, List.of(new CounterSpellEffect())), "counter"));
    }
}
