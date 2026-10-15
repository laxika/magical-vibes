package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SOC", collectorNumber = "15")
@CardRegistration(set = "SOC", collectorNumber = "65")
public class HeraldOfAmity extends Card {

    public HeraldOfAmity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsAndMayCastSpellsEffect(
                        8, null, new CardIsAuraPredicate(), 1, true));

        PermanentCount aurasYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.AURA), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(aurasYouControl, aurasYouControl));
    }
}
