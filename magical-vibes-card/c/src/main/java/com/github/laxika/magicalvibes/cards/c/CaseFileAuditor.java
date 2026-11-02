package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeToCastSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "7")
public class CaseFileAuditor extends Card {

    public CaseFileAuditor() {
        LookAtTopCardsEffect caseSearch = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6, new CardTypePredicate(CardType.ENCHANTMENT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), caseSearch);
        addTrigger(Triggers.youSolveCase(), caseSearch);
        addStaticEffect(new SpendAnyManaTypeToCastSubtypeEffect(CardSubtype.CASE));
    }
}
