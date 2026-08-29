package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "5ED", collectorNumber = "114")
@CardRegistration(set = "ICE", collectorNumber = "92")
@CardRegistration(set = "MIR", collectorNumber = "86")
@CardRegistration(set = "BRB", collectorNumber = "56")
public class RayOfCommand extends Card {

    public RayOfCommand() {
        // Untap target creature an opponent controls and gain control of it until end of turn. That
        // creature gains haste until end of turn. When you lose control of the creature, tap it.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new GainControlOfTargetEffect(ControlDuration.END_OF_TURN, true))
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
