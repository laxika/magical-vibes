package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfOtherSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "22")
@CardRegistration(set = "HOC", collectorNumber = "62")
@CardRegistration(set = "LTC", collectorNumber = "509")
@CardRegistration(set = "LTC", collectorNumber = "553")
public class CallForthTheTempest extends Card {

    public CallForthTheTempest() {
        PermanentPredicate opponentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
        addSpellEffect(new MassDamageEffect(
                new TotalManaValueOfOtherSpellsCastThisTurn(), false, false, opponentCreature));
    }
}
