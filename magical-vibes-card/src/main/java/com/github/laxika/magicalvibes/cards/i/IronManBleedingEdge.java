package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "626")
public class IronManBleedingEdge extends Card {

    public IronManBleedingEdge() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, OncePerTurnTriggerEffect.markOnAcceptance(
                new MayEffect(
                        CopyControllerCastSpellOnSpellCastEffect.permanentSpellToken(
                                new CardTypePredicate(CardType.ARTIFACT), Set.of(CardSupertype.LEGENDARY)),
                        "Copy that spell?")));
    }
}
