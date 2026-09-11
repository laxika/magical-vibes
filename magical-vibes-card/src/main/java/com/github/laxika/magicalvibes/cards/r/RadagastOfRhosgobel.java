package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HOB", collectorNumber = "136")
public class RadagastOfRhosgobel extends Card {

    public RadagastOfRhosgobel() {
        CardPredicate creatureSpell = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.STATIC, new ReduceCastCostForFirstMatchingSpellEachTurnEffect(creatureSpell, 2));
        addEffect(EffectSlot.STATIC, new GrantFlashToFirstMatchingSpellEachTurnEffect(creatureSpell));
    }
}
