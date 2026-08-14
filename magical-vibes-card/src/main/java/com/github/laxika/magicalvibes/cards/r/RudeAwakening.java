package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "92")
public class RudeAwakening extends Card {

    public RudeAwakening() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}{G}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Untap all lands you control",
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate())),
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, lands you control become 2/2 creatures that are still lands",
                        new AnimatePermanentsEffect(
                                2, 2, List.of(), Set.of(), null, Set.of(),
                                GrantScope.OWN_LANDS, EffectDuration.UNTIL_END_OF_TURN))
        )));
    }
}
