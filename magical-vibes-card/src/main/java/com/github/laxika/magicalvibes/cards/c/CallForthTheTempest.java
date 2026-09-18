package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfOtherSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "22")
@CardRegistration(set = "HOC", collectorNumber = "62")
public class CallForthTheTempest extends Card {

    public CallForthTheTempest() {
        PermanentPredicate opponentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                new TotalManaValueOfOtherSpellsCastThisTurn(), false, false, opponentCreature));
    }
}
