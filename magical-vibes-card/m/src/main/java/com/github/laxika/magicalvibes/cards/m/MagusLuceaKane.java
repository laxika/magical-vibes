package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "7")
@CardRegistration(set = "40K", collectorNumber = "174")
public class MagusLuceaKane extends Card {

    public MagusLuceaKane() {
        target(TargetFilters.creature()).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.COLORLESS, 2),
                        RegisterGlobalTriggeredAbilityEffect.nextThisTurn(new TriggeredAbility(List.of(
                                new TriggerCondition(Triggers.youCastSpell()
                                        .withFilter(new CardHasXInManaCostPredicate())),
                                new TriggerCondition(Triggers.youActivateAbility()
                                        .withKind(AbilityTrigger.AbilityKind.WITH_X_IN_COST))),
                                TriggerOptions.DEFAULT,
                                List.of(new EffectRegistration(
                                        new CopyTriggeringSpellOrAbilityEffect().permanentSpellCopiesAreTokens()))))),
                "{T}: Add {C}{C}. When you next cast a spell with {X} in its mana cost or activate an ability with {X} in its activation cost this turn, copy that spell or ability. You may choose new targets for the copy."
        ));
    }
}
