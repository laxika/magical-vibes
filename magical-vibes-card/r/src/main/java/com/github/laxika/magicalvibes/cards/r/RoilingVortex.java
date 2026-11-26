package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "156")
@CardRegistration(set = "SLD", collectorNumber = "1958")
public class RoilingVortex extends Card {

    public RoilingVortex() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 1));

        addTrigger(Triggers.playerCastsSpell().anySpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SpellManaSpentAtLeast(1))),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 5));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new PlayersCantGainLifeThisTurnEffect(Subjects.eachOpponent())),
                "{R}: Your opponents can't gain life this turn."
        ));
    }
}
