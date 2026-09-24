package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "9")
public class RazorDemon extends Card {

    public RazorDemon() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL,
                new DraftFromSpellbookEffect(
                        List.of(
                                new DraftFromSpellbookEffect.SpellbookCard("VOW", "103"),
                                new DraftFromSpellbookEffect.SpellbookCard("C20", "133"),
                                new DraftFromSpellbookEffect.SpellbookCard("ORI", "92")
                        ),
                        DraftFromSpellbookEffect.DraftMode.MAY_CAST_WITHOUT_PAYING_MANA_COST));

        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessDiscardsEffect());
    }
}
