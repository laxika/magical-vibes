package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "59")
public class JinGitaxiasProgressTyrant extends Card {

    public JinGitaxiasProgressTyrant() {
        CardAnyOfPredicate artifactInstantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, OncePerTurnTriggerEffect.keyed(
                new CopyControllerCastSpellOnSpellCastEffect(artifactInstantOrSorcery, null, null), "copy"));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, OncePerTurnTriggerEffect.keyed(
                new SpellCastTriggerEffect(artifactInstantOrSorcery, List.of(new CounterSpellEffect())), "counter"));
    }
}
