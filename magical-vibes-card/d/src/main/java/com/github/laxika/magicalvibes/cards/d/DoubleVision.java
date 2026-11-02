package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "142")
@CardRegistration(set = "M21", collectorNumber = "365")
@CardRegistration(set = "NCC", collectorNumber = "267")
public class DoubleVision extends Card {

    public DoubleVision() {
        addTrigger(Triggers.youCastSpell(),
                CopyControllerCastSpellOnSpellCastEffect.firstMatchingCopy(
                        List.of(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        ))), null));
    }
}
