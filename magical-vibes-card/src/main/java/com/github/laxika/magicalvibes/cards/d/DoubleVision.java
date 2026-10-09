package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "142")
@CardRegistration(set = "M21", collectorNumber = "365")
@CardRegistration(set = "NCC", collectorNumber = "267")
public class DoubleVision extends Card {

    public DoubleVision() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                CopyControllerCastSpellOnSpellCastEffect.firstMatchingCopy(
                        List.of(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        ))), null));
    }
}
