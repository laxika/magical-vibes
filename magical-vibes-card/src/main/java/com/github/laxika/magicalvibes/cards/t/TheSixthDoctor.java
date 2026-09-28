package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "159")
public class TheSixthDoctor extends Card {

    public TheSixthDoctor() {
        // Time Lord's Prerogative — Whenever you cast a historic spell, copy it, except the copy
        // isn't legendary. This ability triggers only once each turn. Permanent spell copies are
        // tokens; the ability does not offer new targets for instant or sorcery copies.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                CopyControllerCastSpellOnSpellCastEffect.firstMatchingCopy(
                        List.of(new CardIsHistoricPredicate()), null,
                        Set.of(CardSupertype.LEGENDARY), false));
    }
}
