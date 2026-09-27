package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.CoolnessAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CoolnessEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ResetCoolnessEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1540")
public class RainbowDash extends Card {

    public RainbowDash() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasKeywordPredicate(Keyword.FLYING),
                                new PermanentHasKeywordPredicate(Keyword.HASTE))),
                        new CoolnessEffect(20)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ConditionalEffect(
                        new CoolnessAtLeast(100),
                        SequenceEffect.of(
                                new AwardManaEffect(ManaColor.WHITE),
                                new AwardManaEffect(ManaColor.BLUE),
                                new AwardManaEffect(ManaColor.BLACK),
                                new AwardManaEffect(ManaColor.RED),
                                new AwardManaEffect(ManaColor.GREEN),
                                new DrawCardEffect(),
                                new ResetCoolnessEffect()))),
                "Sonic Rainboom — {T}: If you're at least 100% cool, add {W}{U}{B}{R}{G}, draw a card, and reset your coolness."
        ));
    }
}
