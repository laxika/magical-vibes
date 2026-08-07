package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "143")
public class ExquisiteFirecraft extends Card {

    public ExquisiteFirecraft() {
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // this spell can't be countered (checked while it is on the stack).
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect(new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        )))));

        // Exquisite Firecraft deals 4 damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
    }
}
