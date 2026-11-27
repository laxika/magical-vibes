package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterChosenNameCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "28")
@CardRegistration(set = "SPM", collectorNumber = "219")
@CardRegistration(set = "OM1", collectorNumber = "33")
@CardRegistration(set = "MSC", collectorNumber = "782")
public class TheCloneSaga extends Card {

    public TheCloneSaga() {
        addTrigger(Triggers.chapter(1), new SurveilEffect(3));
        addTrigger(Triggers.chapter(2), RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                new CardTypePredicate(CardType.CREATURE), List.of(new CopyTriggeringSpellEffect()
                        .withoutNewTargetChoice().permanentSpellCopiesAreTokens().withoutSupertypes(Set.of(CardSupertype.LEGENDARY)))));
        addTrigger(Triggers.chapter(3), new ChooseCardNameAtResolutionEffect(),
                new RegisterChosenNameCombatDamageTriggerEffect(List.of(new DrawCardEffect())));
    }
}
