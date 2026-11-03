package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCompletedDungeon;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "57")
public class EccentricApprentice extends Card {

    public EccentricApprentice() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new VentureIntoDungeonEffect());
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        TriggerOptions.DEFAULT.withInterveningIf(new ControllerHasCompletedDungeon()),
                        SequenceEffect.of(
                                        new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.BIRD),
                                        new SetBasePowerToughnessEffect(1, 1),
                                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)
                                ));
    }
}
