package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreCreaturesThanController;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIC", collectorNumber = "21")
@CardRegistration(set = "FIC", collectorNumber = "136")
public class HeideggerShinraExecutive extends Card {

    public HeideggerShinraExecutive() {
        PermanentCount soldiersYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SOLDIER), CountScope.CONTROLLER);
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new BoostTargetCreatureEffect(soldiersYouControl, new Fixed(0)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                CreateTokenEffect.whiteSoldier(new OpponentsWithMoreCreaturesThanController()));
    }
}
