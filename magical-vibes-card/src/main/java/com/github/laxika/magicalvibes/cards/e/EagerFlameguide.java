package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "YDSK", collectorNumber = "14")
public class EagerFlameguide extends Card {

    public EagerFlameguide() {
        // When Eager Flameguide enters, add {C}{C}{C}. Spend this mana only to cast creature spells.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardRestrictedManaEffect(
                ManaColor.COLORLESS, 3, new ManaRestriction.SpellTypes(Set.of(CardType.CREATURE))));

        // When Eager Flameguide dies, exile the top two cards of your library. Until the end of
        // your next turn, you may cast creature spells from among the exiled cards.
        addEffect(EffectSlot.ON_DEATH, new ExileTopCardsMayCastMatchingUntilNextTurnEffect(
                2, new CardTypePredicate(CardType.CREATURE)));
    }
}
