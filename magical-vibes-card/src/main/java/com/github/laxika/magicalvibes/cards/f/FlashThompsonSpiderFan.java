package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "7")
public class FlashThompsonSpiderFan extends Card {

    public FlashThompsonSpiderFan() {
        PermanentPredicateTargetFilter creatureTarget = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature");
        SpellTarget heckleTarget = target(creatureTarget);
        SpellTarget heroWorshipTarget = target(creatureTarget);
        TapPermanentsEffect tap = new TapPermanentsEffect(TapUntapScope.TARGET);
        UntapPermanentsEffect untap = new UntapPermanentsEffect(TapUntapScope.TARGET);
        registerEffectTargetIndex(tap, heckleTarget.getIndex());
        registerEffectTargetIndex(untap, heroWorshipTarget.getIndex());
        setAllowSharedTargets(true);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                ChooseOneEffect.oneOrMore(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Heckle — Tap target creature.", tap, creatureTarget),
                        new ChooseOneEffect.ChooseOneOption(
                                "Hero Worship — Untap target creature.", untap, creatureTarget)
                ))));
    }
}
