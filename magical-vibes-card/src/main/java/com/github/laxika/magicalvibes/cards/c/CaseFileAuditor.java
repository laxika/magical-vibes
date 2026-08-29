package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeToCastSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MKM", collectorNumber = "7")
public class CaseFileAuditor extends Card {

    public CaseFileAuditor() {
        LookAtTopCardsEffect caseSearch = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6, new CardTypePredicate(CardType.ENCHANTMENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, caseSearch);
        addEffect(EffectSlot.ON_ALLY_CASE_SOLVES, caseSearch);
        addEffect(EffectSlot.STATIC, new SpendAnyManaTypeToCastSubtypeEffect(CardSubtype.CASE));
    }
}
