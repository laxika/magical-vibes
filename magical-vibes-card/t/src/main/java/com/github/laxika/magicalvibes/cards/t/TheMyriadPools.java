package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetPermanentCopyOfTriggeringSpellUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

public class TheMyriadPools extends Card {

    public TheMyriadPools() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE).withProducingSourceForSpellCastTriggers()),
                "{T}: Add {U}."));

        PermanentPredicate otherPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        ControlledPermanentPredicateTargetFilter targetFilter = new ControlledPermanentPredicateTargetFilter(
                otherPermanent,
                "Target must be another permanent you control");
        CardEffect copyEffect = new BecomeTargetPermanentCopyOfTriggeringSpellUntilEndOfTurnEffect();

        target(targetFilter, 0, 1).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardIsPermanentPredicate(),
                        List.of(copyEffect),
                        null,
                        targetFilter,
                        null,
                        false,
                        false,
                        null,
                        0,
                        false,
                        true));
    }
}
