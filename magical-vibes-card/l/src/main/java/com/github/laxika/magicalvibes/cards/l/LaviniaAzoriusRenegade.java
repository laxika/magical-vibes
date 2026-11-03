package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueGreaterThanEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "189")
@CardRegistration(set = "SLD", collectorNumber = "1685")
@CardRegistration(set = "TSR", collectorNumber = "380")
@CardRegistration(set = "RVR", collectorNumber = "195")
@CardRegistration(set = "MB2", collectorNumber = "86")
public class LaviniaAzoriusRenegade extends Card {

    public LaviniaAzoriusRenegade() {
        addStaticEffect(new OpponentsCantCastSpellsWithManaValueGreaterThanEffect(
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.TARGET_PLAYER),
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SpellManaSpentAtLeast(1))),
                new CounterSpellEffect());
    }
}
