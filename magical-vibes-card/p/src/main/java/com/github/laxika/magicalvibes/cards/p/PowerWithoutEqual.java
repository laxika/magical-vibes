package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CastUpToNSpellsFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "DSC", collectorNumber = "352")
public class PowerWithoutEqual extends Card {

    public PowerWithoutEqual() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new DrawCardEffect(3),
                new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.UNTIL_NEXT_TURN),
                new ConditionalEffect(
                        new ControlsPermanentCount(6, new PermanentIsLandPredicate()),
                        new CastUpToNSpellsFromHandWithoutPayingManaCostEffect(3))));
    }
}
