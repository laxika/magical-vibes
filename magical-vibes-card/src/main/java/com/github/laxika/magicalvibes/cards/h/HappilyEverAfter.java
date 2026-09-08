package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CardTypesAmongControlledPermanentsAndGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.ColorsAmongControlledPermanentsAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "16")
public class HappilyEverAfter extends Card {

    public HappilyEverAfter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainLifeEffect(new Fixed(5), GainLifeRecipient.EACH_PLAYER));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachPlayerDrawsCardEffect(1));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new ColorsAmongControlledPermanentsAtLeast(5),
                        new CardTypesAmongControlledPermanentsAndGraveyardAtLeast(6),
                        new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL))),
                new WinGameEffect()));
    }
}
