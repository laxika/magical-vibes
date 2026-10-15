package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "BFZ", collectorNumber = "227")
public class SlabHammer extends Card {

    public SlabHammer() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new ReturnPermanentControlledByPlayerToHandThenEffect(
                        new PermanentIsLandPredicate(),
                        new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2)),
                        "land"),
                "Return a land you control to its owner's hand?"));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
