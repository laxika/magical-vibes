package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "159")
public class SpinerockTyrant extends Card {

    public SpinerockTyrant() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                CopyControllerCastSpellOnSpellCastEffect.withCastTargetCondition(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        new StackEntryIsSingleTargetPredicate(),
                        Set.of(Keyword.WITHER)),
                "Copy that spell?"));
    }
}
