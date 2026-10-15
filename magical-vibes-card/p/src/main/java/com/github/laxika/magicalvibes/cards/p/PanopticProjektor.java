package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "44")
@CardRegistration(set = "MKC", collectorNumber = "354")
public class PanopticProjektor extends Card {

    public PanopticProjektor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ReduceCastCostForNextMatchingSpellEffect(
                        new CardTypePredicate(CardType.CREATURE), 3, true)),
                "{T}: The next face-down creature spell you cast this turn costs {3} less to cast."
        ));
        addEffect(EffectSlot.STATIC, AdditionalTriggeredAbilityEffect.forPermanentTurnsFaceUp());
    }
}
