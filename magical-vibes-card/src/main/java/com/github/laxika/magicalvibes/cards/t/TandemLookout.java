package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;

@CardRegistration(set = "AVR", collectorNumber = "80")
public class TandemLookout extends Card {

    public TandemLookout() {
        // Soulbond (CR 702.95): may pair when this or another unpaired creature enters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Tandem Lookout with another unpaired creature you control?"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new SoulbondPairWithEnteringEffect());

        // While paired, both creatures have "Whenever this creature deals damage to an
        // opponent, draw a card."
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsPaired(),
                new GrantTriggeredAbilityEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                        new DrawCardEffect(1), GrantScope.SELF_AND_PAIRED)));
    }
}
