package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "122")
public class MajesticMyriarch extends Card {

    private static final Set<Keyword> COMBAT_KEYWORDS = Set.of(
            Keyword.FLYING,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.DEATHTOUCH,
            Keyword.HASTE,
            Keyword.HEXPROOF,
            Keyword.INDESTRUCTIBLE,
            Keyword.LIFELINK,
            Keyword.MENACE,
            Keyword.REACH,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    public MajesticMyriarch() {
        // P/T = twice the number of creatures you control (counts itself → at least 2/2).
        Scaled twiceCreatures = new Scaled(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER), 2);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(twiceCreatures, twiceCreatures));

        // At the beginning of each combat, gain each listed keyword UEOT if you control a
        // creature with that keyword. One ability; each keyword checked at resolution.
        for (Keyword keyword : COMBAT_KEYWORDS) {
            addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                    new ConditionalEffect(
                            new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                    new PermanentIsCreaturePredicate(),
                                    new PermanentHasKeywordPredicate(keyword)))),
                            new GrantKeywordEffect(keyword, GrantScope.SELF)));
        }
    }
}
