package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "36")
@CardRegistration(set = "C14", collectorNumber = "88")
public class SilverbladePaladin extends Card {

    public SilverbladePaladin() {
        // Soulbond (CR 702.95): may pair when this or another unpaired creature enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Silverblade Paladin with another unpaired creature you control?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new SoulbondPairWithEnteringEffect());

        // As long as this creature is paired with another creature, both creatures have double strike.
        addStaticEffect(new ConditionalEffect(
                new SourceIsPaired(),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF_AND_PAIRED)));
    }
}
