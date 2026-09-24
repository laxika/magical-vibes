package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasUniqueGreatestPowerAmongAllCreatures;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "320")
@CardRegistration(set = "CMM", collectorNumber = "571")
@CardRegistration(set = "CMM", collectorNumber = "681")
public class SelvalaHeartOfTheWilds extends Card {

    public SelvalaHeartOfTheWilds() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(
                        ConditionalEffect.unless(
                                new TriggeringPermanentHasUniqueGreatestPowerAmongAllCreatures(),
                                new DrawCardForTriggeringPlayerEffect()),
                        "Draw a card?",
                        null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new AwardAnyColorManaEffect(
                        new GreatestPowerAmongControlled(),
                        ManaSpendRestriction.NONE,
                        null,
                        false,
                        false,
                        false,
                        false,
                        true,
                        false)),
                "{G}, {T}: Add X mana in any combination of colors, where X is the greatest power among creatures you control."
        ));
    }
}
