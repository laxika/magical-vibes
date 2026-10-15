package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "257")
public class ReflectiveGolem extends Card {

    public ReflectiveGolem() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CopyControllerCastSpellOnSpellCastEffect(
                instantOrSorcery,
                null,
                "{2}",
                null,
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryIsSingleTargetPredicate(),
                        new StackEntryTargetsSourcePredicate()
                )),
                Set.of(),
                Set.of(),
                false
        ));
    }
}
