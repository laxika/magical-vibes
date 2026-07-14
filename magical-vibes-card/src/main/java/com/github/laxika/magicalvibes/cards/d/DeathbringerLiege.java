package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "85")
public class DeathbringerLiege extends Card {

    public DeathbringerLiege() {
        // Other white creatures you control get +1/+1. (OWN_CREATURES scope excludes the source itself.)
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));

        // Other black creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLACK))));

        // Whenever you cast a white spell, you may tap target creature.
        // (No target filter needed — the may-target path defaults to creatures.)
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.WHITE),
                        List.of(new TapPermanentsEffect(TapUntapScope.TARGET))),
                "Tap target creature?"
        ));

        // Whenever you cast a black spell, you may destroy target creature if it's tapped.
        // Targets any creature; the tapped check happens at resolution (Gatherer ruling).
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLACK),
                        List.of(new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentIsTappedPredicate()),
                                new DestroyTargetPermanentEffect()))),
                "Destroy target creature (if it's tapped)?"
        ));
    }
}
