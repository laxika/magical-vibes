package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "65")
@CardRegistration(set = "LCC", collectorNumber = "97")
public class OrderOfSacredDusk extends Card {

    public OrderOfSacredDusk() {
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new BoostTargetCreatureEffect(1, 1));

        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureYouControlAttacks(),
                        TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                        new BoostTargetCreatureEffect(1, 1)),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)));
    }
}
