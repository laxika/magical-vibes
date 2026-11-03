package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "156")
@CardRegistration(set = "SLD", collectorNumber = "1958")
public class RoilingVortex extends Card {

    public RoilingVortex() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DealDamageToPlayersEffect(1, DamageRecipient.ACTIVE_PLAYER));

        addTrigger(Triggers.playerCastsSpell().anySpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SpellManaSpentAtLeast(1))),
                new DealDamageToPlayersEffect(5, DamageRecipient.TRIGGERING_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new OpponentsCantGainLifeThisTurnEffect()),
                "{R}: Your opponents can't gain life this turn."
        ));
    }
}
