package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeThisTurnEffect;

@CardRegistration(set = "MOM", collectorNumber = "52")
@CardRegistration(set = "MOM", collectorNumber = "351")
public class CompleteTheCircuit extends Card {

    public CompleteTheCircuit() {
        addSpellEffect(new GrantFlashToCardTypeThisTurnEffect(CardType.SORCERY));
        addSpellEffect(
                RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                        CardPredicateUtils.instantOrSorcery(), List.of(new CopyTriggeringSpellEffect().withCopies(2))));
    }
}
