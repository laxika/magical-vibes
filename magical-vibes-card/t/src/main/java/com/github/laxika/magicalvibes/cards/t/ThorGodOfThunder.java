package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "156")
public class ThorGodOfThunder extends Card {

    public ThorGodOfThunder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        )),
                        true
                ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new DealDamageEqualToSpellManaValueToAnyTargetEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))
                ));
    }
}
