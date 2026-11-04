package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceCardInExile;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "3")
public class TawnosEndures extends Card {

    public TawnosEndures() {
        target(TargetFilters.creature())
                .addSpellEffect(new PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
                                new TriggeredAbility(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.EXILE),
                                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInExile()),
                                SequenceEffect.of(
                                        new PerpetuallyBoostSourceEffect(1, 1),
                                        new MayEffect(new ReturnSourceCardFromExileToBattlefieldEffect(false),
                                                "Return it to the battlefield?")))))
                .addSpellEffect(new ExileTargetPermanentEffect());
    }
}
