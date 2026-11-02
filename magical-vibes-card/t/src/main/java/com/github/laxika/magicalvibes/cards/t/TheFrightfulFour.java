package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "44")
@CardRegistration(set = "MSC", collectorNumber = "349")
public class TheFrightfulFour extends Card {

    public TheFrightfulFour() {
        addTrigger(Triggers.opponentCastsSpell(), SpellCastTriggerEffect.nth(
                1,
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new LoseLifeEffect(new TargetSpellManaValue(), LoseLifeRecipient.TRIGGERING_PLAYER))
        ));
    }
}
