package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "10")
@CardRegistration(set = "OTC", collectorNumber = "46")
public class AngelicSellSword extends Card {

    public AngelicSellSword() {
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                                ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(mercenaryToken()))));

        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourcePowerAtLeast(6)),
                new DrawCardEffect(1));
    }

    private static CreateTokenEffect mercenaryToken() {
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Mercenary", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.MERCENARY), Set.of(), Set.of(), false, false, List.of(),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new BoostTargetCreatureEffect(1, 0)),
                        "{T}: Target creature you control gets +1/+0 until end of turn. Activate only as a sorcery.",
                        TargetFilters.creatureYouControl(),
                        null,
                        null,
                        ActivationTimingRestriction.SORCERY_SPEED
                )),
                false, false, false, 0, Set.of());
    }
}
