package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "106")
public class Hylderblade extends Card {

    public Hylderblade() {
        addStaticEffect(new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new VoidCondition(), new AttachSourceEquipmentToTargetCreatureEffect()));

        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
