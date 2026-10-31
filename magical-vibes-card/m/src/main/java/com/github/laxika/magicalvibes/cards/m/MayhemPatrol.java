package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "114")
public class MayhemPatrol extends Card {

    public MayhemPatrol() {
        addCastingOption(AlternateHandCast.blitz("{1}{R}"));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(1, 0));

        // Blitz (CR 702.152a): haste while the blitz cost was paid; the end-step sacrifice is
        // created as the spell resolves.
        addStaticEffect(new ConditionalEffect(
                new CastForAlternateCost(), new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
        addEffect(EffectSlot.ON_DEATH, new ConditionalEffect(
                new CastForAlternateCost(), new DrawCardEffect()));
    }
}
