package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "208")
public class NajalTheStormRunner extends Card {

    public NajalTheStormRunner() {
        addStaticEffect(new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay("{2}", RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                        CardPredicateUtils.instantOrSorcery(), List.of(new CopyTriggeringSpellEffect()))).withPrompt("Pay {2} to copy your next instant or sorcery spell this turn?"));
    }
}
