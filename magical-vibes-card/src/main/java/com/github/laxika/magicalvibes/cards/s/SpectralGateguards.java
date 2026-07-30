package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;

@CardRegistration(set = "AVR", collectorNumber = "37")
public class SpectralGateguards extends Card {

    public SpectralGateguards() {
        // Soulbond (CR 702.95): may pair when this or another unpaired creature enters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Spectral Gateguards with another unpaired creature you control?"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new SoulbondPairWithEnteringEffect());

        // As long as this creature is paired with another creature, both creatures have vigilance.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsPaired(),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF_AND_PAIRED)));
    }
}
