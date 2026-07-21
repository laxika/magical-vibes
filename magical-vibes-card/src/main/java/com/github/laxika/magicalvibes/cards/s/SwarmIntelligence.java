package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "50")
public class SwarmIntelligence extends Card {

    public SwarmIntelligence() {
        // Whenever you cast an instant or sorcery spell, you may copy that spell.
        // You may choose new targets for the copy. No cost — reuses Aziza's copy machinery
        // (no tap or mana cost) wrapped in MayEffect for the free "you may" prompt.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new CopyControllerCastSpellOnSpellCastEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        )),
                        null, null),
                "Copy that spell?"));
    }
}
