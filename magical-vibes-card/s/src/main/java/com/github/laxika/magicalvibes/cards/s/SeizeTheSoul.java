package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "61")
public class SeizeTheSoul extends Card {

    public SeizeTheSoul() {
        target(nonWhiteNonBlackCreature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(1));
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_DEATH, new HauntEffect());
        target(nonWhiteNonBlackCreature())
                .addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, CreateTokenEffect.whiteSpirit(1));
    }

    private static PermanentPredicateTargetFilter nonWhiteNonBlackCreature() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.WHITE))),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                "Target must be a nonwhite, nonblack creature"
        );
    }
}
